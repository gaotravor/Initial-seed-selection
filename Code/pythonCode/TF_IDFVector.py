import math
import os
import numpy as np
import pandas as pd
import javalang.tokenizer
from javalang.tokenizer import tokenize
import time
FILE_SEPARATOR = os.path.sep


def tokenize_java_code(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        code = file.read()
    # 分词
    tokens = tokenize(code)
    # 删除tokens中的分隔符
    tokens = [token for token in tokens if not isinstance(token, javalang.tokenizer.Separator)]
    tokens = [token.value for token in tokens]
    return tokens


def constructing_vocabulary(file_list_path):
    if VOCABULARY is not None:
        return VOCABULARY
    # 读取文件中的代码文件路径
    with open(file_list_path, 'r', encoding='utf-8') as file:
        file_paths = [line.strip() for line in file.readlines()]

    # 构建词汇表
    vocabulary = set()

    for file_path in file_paths:
        tokens = tokenize_java_code(file_path)
        vocabulary.update(tokens)

    return vocabulary


def calculate_tf(file_path):
    tokens = tokenize_java_code(file_path)

    # 统计词频
    word_count = {}
    for token in tokens:
        if token in word_count:
            word_count[token] += 1
        else:
            word_count[token] = 1

    # 计算TF值
    tf_values = {}
    total_words = len(tokens)
    for token, count in word_count.items():
        tf_values[token] = count / total_words

    return tf_values


def calculate_idf(file_list_path):
    if IDF_VALUES is not None:
        return IDF_VALUES
    vocabulary = constructing_vocabulary(file_list_path)
    total_documents = len(open(file_list_path, encoding='utf-8').readlines())

    # 统计文档频率（DF）
    document_frequency = {word: 0 for word in vocabulary}
    with open(file_list_path, 'r', encoding='utf-8') as file_list:
        for line in file_list:
            line = line.strip()
            tokens = tokenize_java_code(line)
            unique_tokens = set(tokens)
            for token in unique_tokens:
                document_frequency[token] += 1

    # 计算IDF值
    idf_values = {}
    for word, df in document_frequency.items():
        idf_values[word] = math.log(total_documents / (df + 1))

    return idf_values


def calculate_tf_idf(file_path, file_list_path):
    tf_values = calculate_tf(file_path)
    idf_values = calculate_idf(file_list_path)

    tf_idf_values = {}
    for word in tf_values:
        tf_idf_values[word] = tf_values[word] * idf_values.get(word, 0)

    return tf_idf_values


def build_feature_vectors(file_list_path):
    vocabulary = constructing_vocabulary(file_list_path)

    feature_vectors = {}

    for line in open(file_list_path, 'r', encoding='utf-8'):
        line = line.strip()
        file_path = line

        tf_idf_values = calculate_tf_idf(file_path, file_list_path)

        # 构建特征向量
        feature_vector = [tf_idf_values.get(word, 0) for word in vocabulary]
        feature_vectors[line] = feature_vector

    return feature_vectors


start_time = time.time()
rootPath = "Z:/JVM_Testing/SeedSelector"
if not os.path.exists(rootPath):
    rootPath = "../../"
project = sys.argv[1]
codePath = rootPath + FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "tmp" + FILE_SEPARATOR + project + FILE_SEPARATOR + "code"
featurePath = rootPath + FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "featureInfo" + FILE_SEPARATOR + project
if not os.path.exists(featurePath):
    os.makedirs(featurePath)
# 将feature/code文件夹下的所有文件路径写入feature/TF_IDF_code_list.txt文件中
tmpPath = rootPath + FILE_SEPARATOR + "Data" + FILE_SEPARATOR + "tmp" + FILE_SEPARATOR + project
with open(tmpPath + FILE_SEPARATOR + 'TF_IDF_code_list.txt', 'w', encoding='utf-8') as file:
    for root, dirs, files in os.walk(tmpPath + FILE_SEPARATOR + 'code'):
        for file_name in files:
            file.write(os.path.join(root, file_name) + '\n')

fileListPath = tmpPath + FILE_SEPARATOR + 'TF_IDF_code_list.txt'

VOCABULARY = None
IDF_VALUES = None
VOCABULARY = constructing_vocabulary(fileListPath)
IDF_VALUES = calculate_idf(fileListPath)

vectors = []
for featureVector in build_feature_vectors(fileListPath).items():
    # 如果系统是windows
    if FILE_SEPARATOR == "\\":
        vector = [featureVector[0].split("\\")[-1]]
    else:
        vector = [featureVector[0].split("/")[-1]]
    vector[0] = vector[0].replace(".java", "")
    for i in featureVector[1]:
        vector.append(i)

    vectors.append(vector)
pd.DataFrame(np.array(vectors)).to_csv(featurePath + FILE_SEPARATOR + "TF_IDFVectors.csv")
end_time = time.time()
print("TF_IDFVector.py运行时间：" + str(end_time - start_time))