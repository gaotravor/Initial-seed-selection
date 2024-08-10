#!/bin/bash

# 定义函数用于执行命令
run_command() {
    local source_file=$1
    local target_dir_1=$2
    local target_dir_2=$3
    local log_number=$4
    local project_name=$5
    local entry_name=$6
    local random_seed=$7
    local exec_mode=$8

    cp "/home/share/subset/$project_name/$source_file" "02Benchmarks/$target_dir_1/testcases.txt"
    cp "/home/share/subset/$project_name/$source_file" "sootOutput/$target_dir_1/testcases.txt"
 
    if [ "$exec_mode" == "VECT" ]; then
        nohup java -XX:+UseStringDeduplication -Xms1024m -Xmx10240m -cp ./VECT.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar "$entry_name" -r "$random_seed" -s "$target_dir_1" -p "$target_dir_2" -sl rws -cl infercode -ch false -et 86400 -cov false > "./03results/$log_number.log" &
    fi

    if [ "$exec_mode" == "JavaTailor" ]; then
        nohup java -XX:+UseStringDeduplication -Xms1024m -Xmx10240m -cp ./VECT.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar "$entry_name" -r "$random_seed" -s "$target_dir_1" -p "$target_dir_2" -sl random -cl no_cluster -ch false -et 86400 -cov false > "./03results/$log_number.log" &
    fi
    echo ""

    # 等待5秒
    sleep 5
}

# 检查参数数量
if [ "$#" -ne 5 ]; then
    echo "Usage: $0 <project_name> <entry_name> <random_seed> <exec_mode> <source_file_path>"
    exit 1
fi

project_name=$1
entry_name=$2
random_seed=$3
exec_mode=$4
source_file_path=$5

# 从文件中读取源文件列表
source_files=($(cat "$source_file_path"))

# 获取源文件个数
num_files=${#source_files[@]}

# 定义目标目录数组
declare -a target_dirs
declare -a target_dirs_2

# 循环执行命令
for i in "${!source_files[@]}"; do
    if [ "$project_name" == "HotspotTests-Java" ]; then
        target_dirs+=("$project_name$((($i)*2+1))")
        target_dirs_2+=("$project_name$((($i)*2+2))")
    else
        target_dirs+=("$project_name$(($i+1))")
        target_dirs_2+=("HotspotTests-Java$(($i+1))")
    fi
    
    source_file=$(echo "${source_files[$i]}" | tr -d '\r')

    run_command "$source_file" "${target_dirs[$i]}" "${target_dirs_2[$i]}" "$((i+1))" "$project_name" "$entry_name" "$random_seed" "$exec_mode"
done

