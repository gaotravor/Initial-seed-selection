import time

from infercode.client.infercode_client import InferCodeClient
import logging
import numpy as np
import pandas as pd
import os
from tqdm import tqdm

logging.basicConfig(level=logging.INFO)

os.environ["CUDA_VISIBLE_DEVICES"] = "-1"

FILE_SEPARATOR = os.path.sep

inferCode = InferCodeClient(language="java")
inferCode.init_from_config()


def java2vec(folder_path, filename):
    code = ""
    with open(folder_path + FILE_SEPARATOR + filename, 'r', encoding='utf-8') as file:
        code = file.read()
    javaCode = ""
    for line in code.split("\n"):
        javaCode += line.strip()
    if len(javaCode) > 100000:
        return []
    vector = inferCode.encode([javaCode])
    vector = vector[0]
    return vector


start_time = time.time()
vectors = []
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
    pd.DataFrame(np.array(vectors)).to_csv(featurePath + FILE_SEPARATOR + "InferCodeVectors.csv")
end_time = time.time()
print("InferCode Time: " + str(end_time - start_time))
