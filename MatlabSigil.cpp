// MatlabSigil.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"
#include "MatlabSigil.h"
#include "sigil_shm.h"
#include "jni.h"

BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved)
{
    return TRUE;
}


static double *matlabVecor;
static int nInputs;

JNIEXPORT void JNICALL Java_Matlab_pollMatlabNative(JNIEnv *, jobject)
{
  
}


JNIEXPORT void JNICALL Java_InterTrax_initMatlab
  (JNIEnv *, jobject)
{
  shm = (union SigilSHM *) sigil_make_shm(SHM_NAME, SHM_SIZE);
  memset(shm->array, SHM_SIZE*sizeof(shm->array[k]), 0);

  matlabVector = malloc(sizeof(double)*2);
  nInputs = 2;

}

JNIEXPORT jdoubleArray JNICALL Java_Matlab_getMatlabVector(JNIEnv * env, jobject)
{

	int i;
	jdoubleArray retArray = NewDoubleArray(env, nInputs);
	jdouble *elts = getDoubleArrayElements(env, retArray, NULL);
	for(i=0;i<nInputs;i++)
		elts[i] = matlabVector[i];
	
	return retArray;
}

