#ifndef __WORD2VECTOR_H
#define __WORD2VECTOR_H
#endif

extern "C" __declspec(dllexport) int Init(char *word2vecFilePath);

extern "C" __declspec(dllexport) int Word2VecLoad(char **rWords, int *rWordsLength, float** rConfidences, int *rNumberWords, char *word);

extern "C" __declspec(dllexport) void FreeMemoryString(char** pData);

extern "C" __declspec(dllexport) void FreeMemoryFloats(float** pData);
