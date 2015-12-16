// This code is an adaptation of https://code.google.com/p/word2vec/
#include "Word2Vector.h"
#include <stdio.h>
#include <string>
#include <math.h>
#include <malloc.h>

const int max_size = 2000;         // max length of strings
const int N = 10;				  // number of closest words that will be shown
int max_w = 50;
long long words, size;
char *vocab;
float *M;

extern "C" __declspec(dllexport) int Init(char *word2vecFilePath)
{

	FILE *f;
	char st1[max_size];
	char bestw[N][max_size];
	char file_name[max_size], st[100][max_size];
	float dist, len, bestd[N];
	long long a, b, c, d, cn, bi[100];
	char ch;



	strcpy(file_name, word2vecFilePath);
	f = fopen(file_name, "rb");
	if (f == NULL) {
		return -1;
	}

	fscanf(f, "%lld", &words); words = 354991;
	
	fscanf(f, "%lld", &size);
	vocab = (char *)malloc((long long)words * max_w * sizeof(char));
	M = (float *)malloc((long long)words * (long long)size * sizeof(float));
	if (M == NULL) {
		return -2;
	}

	for (b = 0; b < words; b++) {
		a = 0;
		while (1) {
			vocab[b * max_w + a] = fgetc(f);
			if (feof(f) || (vocab[b * max_w + a] == ' ')) break;
			if ((a < max_w) && (vocab[b * max_w + a] != '\n')) a++;
		}
		vocab[b * max_w + a] = 0;
		for (a = 0; a < size; a++)
			fread(&M[a + b * size], sizeof(float), 1, f);

		len = 0;
		for (a = 0; a < size; a++) len += M[a + b * size] * M[a + b * size];
		len = sqrt(len);
		for (a = 0; a < size; a++) M[a + b * size] /= len;
	}
	fclose(f);

	return 0;
}
#include <iostream>
#include <fstream>


extern "C" __declspec(dllexport) int Word2VecLoad(char **rWords, int *rWordsLength, float** rConfidences, int *rNumberWords, char *word)
{

	long long a, b, c, d, cn, bi[100];
	char bestw[N][max_size];
	float dist, bestd[N], len, vec[max_size];
	char st[100][max_size];
	char st1[max_size];

	*rNumberWords = N;

		for (a = 0; a < N; a++) bestd[a] = 0;
		for (a = 0; a < N; a++) bestw[a][0] = 0;
		
		std::string aux1 = std::string(word) + " " + word + " " + word;
		strcpy(st1, aux1.c_str());


		cn = 0;
		b = 0;
		c = 0;
		while (1) {
			st[cn][b] = st1[c];
			b++;
			c++;
			st[cn][b] = 0;
			if (st1[c] == 0) break;
			if (st1[c] == ' ') {
				cn++;
				b = 0;
				c++;
			}
		}
		cn++;
		
		for (a = 0; a < cn; a++) {
			for (b = 0; b < words; b++) if (!strcmp(&vocab[b * max_w], st[a])) break;
			if (b == words) b = 0;
			bi[a] = b;
			if (b == 0) {
				break;
			}
		}
		if (b == 0) return -3;
		
		for (a = 0; a < size; a++) vec[a] = M[a + bi[1] * size] - M[a + bi[0] * size] + M[a + bi[2] * size];
		len = 0;
		for (a = 0; a < size; a++) len += vec[a] * vec[a];
		len = sqrt(len);
		for (a = 0; a < size; a++) vec[a] /= len;
		for (a = 0; a < N; a++) bestd[a] = 0;
		for (a = 0; a < N; a++) bestw[a][0] = 0;
		for (c = 0; c < words; c++) {
			if (c == bi[0]) continue;
			if (c == bi[1]) continue;
			if (c == bi[2]) continue;
			a = 0;
			for (b = 0; b < cn; b++) if (bi[b] == c) a = 1;
			if (a == 1) continue;
			dist = 0;
			for (a = 0; a < size; a++) dist += vec[a] * M[a + c * size];
			for (a = 0; a < N; a++) {
				if (dist > bestd[a]) {
					for (d = N - 1; d > a; d--) {
						bestd[d] = bestd[d - 1];
						strcpy(bestw[d], bestw[d - 1]);
					}
					bestd[a] = dist;
					strcpy(bestw[a], &vocab[c * max_w]);
					break;
				}
			}
		}
		std::string aux = "";
		(*rConfidences) = (float*) malloc(10 * sizeof(float));
		for (a = 0; a < N; a++)
		{
			aux = aux+  "#" + bestw[a];
			(*rConfidences)[a] = bestd[a];
		}

		(*rWords) = new char[aux.size() + 1];
		*rWordsLength = aux.size();
		memcpy((*rWords), aux.c_str(), aux.size());
		(*rWords)[aux.size()] = '\0';

	return 0;
}

void FreeMemoryString(char** pData){
		delete [] (*pData);
}

void FreeMemoryFloats(float** pData){
		delete[] (*pData);
}

void main()
{
	char *rWords = 0;
	float *rConfidences = 0;
	int rWordsLength = 0, rNumberWords = 0;
	char *word2vecFilePath = "E:\\bases\\sentimentanalysis\\word2vec_twitter_model\\word2vec_twitter_model.bin";
	char* word = "coke";

	Init(word2vecFilePath);
	Word2VecLoad(&rWords, &rWordsLength, &rConfidences, &rNumberWords, word);
	FreeMemoryString(&rWords);
	FreeMemoryFloats(&rConfidences);
}