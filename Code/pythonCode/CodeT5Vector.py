import time

import torch.nn as nn
import numpy as np
import os
import torch
import logging
import pandas as pd
from transformers import (RobertaConfig, RobertaModel, RobertaTokenizer,
                          BartConfig, BartForConditionalGeneration, BartTokenizer,
                          T5Config, T5ForConditionalGeneration, T5Tokenizer)
from tqdm import tqdm

FILE_SEPARATOR = os.path.sep
logging.basicConfig(level=logging.INFO)
os.environ["CUDA_VISIBLE_DEVICES"] = "-1"
modelPath = "C:/Users/ningmo/.cache/huggingface/codet5-base"
if not os.path.exists(modelPath):
    modelPath = "../../codet5-base"


def build_or_load_gen_model(args):
    config_class, model_class, tokenizer_class = MODEL_CLASSES['codet5']
    config = config_class.from_pretrained(args.config_name if args.config_name else args.model_name_or_path)
    tokenizer = tokenizer_class.from_pretrained(args.tokenizer_name)
    if args.model_type == 'roberta':
        encoder = model_class.from_pretrained(args.model_name_or_path, config=config)
        decoder_layer = nn.TransformerDecoderLayer(d_model=config.hidden_size, nhead=config.num_attention_heads)
        decoder = nn.TransformerDecoder(decoder_layer, num_layers=6)
        model = Seq2Seq(encoder=encoder, decoder=decoder, config=config,
                        beam_size=args.beam_size, max_length=args.max_target_length,
                        sos_id=tokenizer.cls_token_id, eos_id=tokenizer.sep_token_id)
    else:
        model = model_class.from_pretrained(args.model_name_or_path)

    logger.info("Finish loading model [%s] from %s", get_model_size(model), args.model_name_or_path)

    if args.load_model_path is not None:
        logger.info("Reload model from {}".format(args.load_model_path))
        model.load_state_dict(torch.load(args.load_model_path))

    return config, model, tokenizer


class RobertaClassificationHead(nn.Module):
    """Head for sentence-level classification tasks."""

    def __init__(self, config):
        super().__init__()
        self.dense = nn.Linear(config.hidden_size * 2, config.hidden_size)
        self.out_proj = nn.Linear(config.hidden_size, 2)

    def forward(self, x, **kwargs):
        x = x.reshape(-1, x.size(-1) * 2)
        x = self.dense(x)
        x = torch.tanh(x)
        x = self.out_proj(x)
        return x


class CloneModel(nn.Module):
    def __init__(self, encoder, config, tokenizer):
        super(CloneModel, self).__init__()
        self.encoder = encoder
        self.config = config
        self.tokenizer = tokenizer
        self.classifier = RobertaClassificationHead(config)

    def get_t5_vec(self, source_ids):
        attention_mask = source_ids.ne(self.tokenizer.pad_token_id)
        outputs = self.encoder(input_ids=source_ids, attention_mask=attention_mask,
                               labels=source_ids, decoder_attention_mask=attention_mask, output_hidden_states=True)
        hidden_states = outputs['decoder_hidden_states'][-1]
        eos_mask = source_ids.eq(self.config.eos_token_id)

        if len(torch.unique(eos_mask.sum(1))) > 1:
            raise ValueError("All examples must have the same number of <eos> tokens.")
        vec = hidden_states[eos_mask, :].view(hidden_states.size(0), -1,
                                              hidden_states.size(-1))[:, -1, :]
        return vec

    def get_bart_vec(self, source_ids):
        attention_mask = source_ids.ne(self.tokenizer.pad_token_id)
        outputs = self.encoder(input_ids=source_ids, attention_mask=attention_mask,
                               labels=source_ids, decoder_attention_mask=attention_mask, output_hidden_states=True)
        hidden_states = outputs['decoder_hidden_states'][-1]
        eos_mask = source_ids.eq(self.config.eos_token_id)

        if len(torch.unique(eos_mask.sum(1))) > 1:
            raise ValueError("All examples must have the same number of <eos> tokens.")
        vec = hidden_states[eos_mask, :].view(hidden_states.size(0), -1,
                                              hidden_states.size(-1))[:, -1, :]
        return vec

    def get_roberta_vec(self, source_ids):
        attention_mask = source_ids.ne(self.tokenizer.pad_token_id)
        vec = self.encoder(input_ids=source_ids, attention_mask=attention_mask)[0][:, 0, :]
        return vec

    def forward(self, source_ids=None, labels=None):
        source_ids = source_ids.view(-1, self.args.max_source_length)

        if self.args.model_type == 'codet5':
            vec = self.get_t5_vec(source_ids)
        elif self.args.model_type == 'bart':
            vec = self.get_bart_vec(source_ids)
        elif self.args.model_type == 'roberta':
            vec = self.get_roberta_vec(source_ids)

        logits = self.classifier(vec)
        prob = nn.functional.softmax(logits)

        if labels is not None:
            loss_fct = nn.CrossEntropyLoss()
            loss = loss_fct(logits, labels)
            return loss, prob
        else:
            return prob


def java2vec(folder_path, filename):
    code = ""
    with open(folder_path + FILE_SEPARATOR + filename, 'r', encoding='utf-8') as file:
        code = file.read()

    MODEL_CLASSES = {'roberta': (RobertaConfig, RobertaModel, RobertaTokenizer),
                     't5': (T5Config, T5ForConditionalGeneration, T5Tokenizer),
                     'codet5': (T5Config, T5ForConditionalGeneration, RobertaTokenizer),
                     'bart': (BartConfig, BartForConditionalGeneration, BartTokenizer)}

    javaCode = ""
    for line in code.split("\n"):
        javaCode += line.strip()
    vector = []
    snippetCount = 0
    while len(javaCode) != 0:
        snippetCount = snippetCount + 1

        if len(javaCode) >= 512:
            snippet = javaCode[0:512]
            javaCode = javaCode[512:len(javaCode)]
        else:
            snippet = javaCode
            javaCode = ""

        model = CloneModel(encoder, config, tokenizer)
        input_ids = tokenizer.encode(snippet, return_tensors="pt")
        snippetVector = model.get_t5_vec(input_ids)
        snippetVector = snippetVector[0].detach().numpy()

        if len(vector) == 0:
            vector = snippetVector
        else:
            vector = vector + snippetVector
    vector = vector / snippetCount
    return vector


start_time = time.time()
vectors = []
config = T5Config.from_pretrained(modelPath)
tokenizer = RobertaTokenizer.from_pretrained(modelPath)
encoder = T5ForConditionalGeneration.from_pretrained(modelPath)
rootPath = "Z:/JVM_Testing/SeedSelector"
if not os.path.exists(rootPath):
    rootPath = "../../"
project = sys.argv[1]
codePath = rootPath + FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "tmp" + FILE_SEPARATOR + project + FILE_SEPARATOR + "code"
featurePath = rootPath + FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "featureInfo" + FILE_SEPARATOR + project
if not os.path.exists(featurePath):
    os.makedirs(featurePath)
for fileName in tqdm(os.listdir(codePath)):
    vector = [fileName]
    # 如果系统是windows
    if FILE_SEPARATOR == "\\":
        vector = [fileName.split("\\")[-1]]
    else:
        vector = [fileName.split("/")[-1]]
    vector[0] = vector[0].replace(".txt", "")
    result = java2vec(codePath, fileName)
    if len(result) == 0:
        continue
    for value in result:
        vector.append(value)
    vectors.append(vector)
    pd.DataFrame(np.array(vectors)).to_csv(featurePath + FILE_SEPARATOR + "CodeT5Vectors.csv")
end_time = time.time()
print("CodeT5 Time: " + str(end_time - start_time))
