import os
import time
import sys
from transformers import AutoTokenizer, AutoModel, AutoConfig
import torch
import numpy as np
import logging
import pandas as pd
from tqdm import tqdm

FILE_SEPARATOR = os.path.sep
logging.basicConfig(level=logging.INFO)
modelPath = "C:/Users/ningmo/.cache/huggingface/codebert-base"
if not os.path.exists(modelPath):
    modelPath = "../../codebert-base"


def java2vec(folder_path, filename):
    code = ""
    with open(folder_path + FILE_SEPARATOR + filename, 'r', encoding='utf-8') as file:
        code = file.read()

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

        nl_tokens = tokenizer.tokenize("")
        code_tokens = tokenizer.tokenize(snippet)
        tokens = [tokenizer.cls_token] + nl_tokens + [tokenizer.sep_token] + code_tokens + [tokenizer.sep_token]
        tokens_ids = tokenizer.convert_tokens_to_ids(tokens)
        input_tensor = torch.tensor(tokens_ids)[None, :]
        if input_tensor.shape[1] > 514:
            continue
        context_embeddings = model(input_tensor)[0]
        snippetVector = context_embeddings[0][0].detach().numpy()

        if len(vector) == 0:
            vector = snippetVector
        else:
            vector = vector + snippetVector
    vector = vector / snippetCount
    return vector


start_time = time.time()
vectors = []
tokenizer = AutoTokenizer.from_pretrained(modelPath)
model = AutoModel.from_pretrained(modelPath)
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
    pd.DataFrame(np.array(vectors)).to_csv(featurePath + FILE_SEPARATOR + "CodeBERTVectors.csv")
end_time = time.time()
print("CodeBERT Time: " + str(end_time - start_time))
