#!/usr/bin/python
#coding: utf-8
import csv
import os,sys
import pandas as pd

try:
    # path_input = 'data/root/output/abdera/storage'
    path_input = sys.argv[1]
    # path_output = 'data/result'
    path_output = sys.argv[2]
    # project = 'abdera'
    project = sys.argv[3]

    print('----------------------')
    print('init process..')
    print('----------------------')

    def read_file_java(path):
        list_of_lists = []
        with open(path) as f:
            for line in f:
                inner_list = [elt.strip() for elt in line.split(',')]
                # in alternative, if you need to use the file content as numbers
                # inner_list = [int(elt.strip()) for elt in line.split(',')]
                list_of_lists.append(inner_list)
        return list_of_lists

    def list_files(startpath):
        result = []
        count = 0
        for root, dirs, files in os.walk(startpath):
            level = root.replace(startpath, '').count(os.sep)
            indent = ' ' * 4 * (level)
            if level == 1:
                hash_git = root.replace(startpath,'').replace('/','')
            subindent = ' ' * 4 * (level + 1)
            for f in files:
                full_path = os.path.join(root,f)
                rd_file = read_file_java(full_path)
                count += 1
                # print('read {} file...'.format(f))
                result.append({'hash' : hash_git, 'full_path' : full_path, 'file' : rd_file})
        print('read {} files...'.format(count))
        print('----------------------')
        return result


    data = list_files(path_input)
    df = pd.DataFrame(data,columns=['hash','full_path','file'])
    path_save = "{}/{}.csv".format(path_output,project)
    if not os.path.isdir(path_output):
        os.mkdir(path_output)
    df.to_csv(path_save)


    print('end process..')
    print('----------------------')
except IndexError:
    print('required input args <inputDir> <outputDir> <projectName>')
    print('e.g.: python3 sourcecode2csv.py repostory/project results my_project')
