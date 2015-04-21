import os

searchPath = 'D:\\po-znaika\\Alphabet-client\\Alphabet-Data\\final_images\\Images_compressed'
postfix = 'uppercase'


allFiles = os.listdir(searchPath)

for fileName in allFiles:
    index = fileName.find(postfix)
    if index >= 0:
        newFileName = fileName[0:index] + fileName[index + len(postfix):]
        os.rename(searchPath + '\\' + fileName, searchPath + '\\' + newFileName)
        
