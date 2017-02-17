public class FFT
{

    private double [] real;
    private double [] imag;
    private double [] amp;
    private int nSamples;
    private static final double numerator = Math.PI * 2.0;

    public double [] getAmplitudeSpectrum()
    {
	return amp;
    }

    
    public int reverseBits(int n, int nBits)
    {
	int rev = 0;
	for(int i=0;i<nBits;i++)
	    {
		rev = (rev << 1) | (n & 1);
		n >>= 1;
	    }
	return rev;
    }

    public double getDominant(int windowSize, int startPt, 
			      double samplingRate)
    {
	int n = nSamples>>1;
	for(int i=0;i<startPt;i++)
	    amp[i] = 0.0;
	
	double maxWeight = -1.0;
	double centreFreq = -1.0;

	for(int i2 = windowSize; i2<n; i2++)
	    {
		int i1 = i2 - windowSize;
		double moment = 0.0, weight = 0.0;
		for(int i=i1;i<=i2;i++)
		    {
			weight += amp[i];
			moment += (double)i * amp[i];
		    }
		if(weight>maxWeight)
		    {
			maxWeight = weight;
			centreFreq = moment/weight;
		    }
	    }
	return samplingRate * centreFreq / (double)nSamples;
    }

    private void doFFT(double [] timeSeries)
    {
	int numBits = (int)(Math.log(nSamples)/Math.log(2));

	for(int i = 0;i<nSamples;i++)
	    {
		int j = reverseBits(i,numBits);
		real[j] = timeSeries[i];
		imag[i] = 0.0;
	    }

	int blockEnd = 1;
	
	int k;

	for(int blockSize = 2; blockSize <= nSamples; blockSize <<= 1)
	    {
		double dAngle = numerator / (double) blockSize;
		double sm1 = Math.sin ( -dAngle);
		double sm2 = Math.sin ( -2 * dAngle);
		double cm1 = Math.cos ( -dAngle);
		double cm2 = Math.cos ( -2 * dAngle);
		double w = 2 * cm1;
		double [] ar = new double[3];
		double [] ai = new double[3];

		double temp;
		
		for(int i=0;i<nSamples;i+=blockSize)
		    {			
			ai[1] = sm1;
			ai[2] = sm2;

			ar[1] = cm1;
			ar[2] = cm2;
			
			int n=0;
			for(int j=i; n<blockEnd; j++, n++)
			    {
				ar[0] = w * ar[1] - ar[2];
				ar[2] = ar[1];
				ar[1] = ar[0];

				ai[0] = w*ai[1] - ai[2];
				ai[2] = ai[1];
				ai[1] = ai[0];

				k = j + blockEnd;
				double tr = ar[0]*real[k] - ai[0]*imag[k];
				double ti = ar[0]*imag[k] + ai[0]*real[k];

				real[k] = real[j] - tr;
				imag[k] = imag[j] - ti;

				real[j] += tr;
				imag[j] += ti;
			    }
			
		    }
		blockEnd = blockSize;
	    }

	amp = new double[nSamples>>1];
	for(int i=0;i<nSamples>>1;i++)
	    amp[i] = real[i]*real[i] + imag[i]*imag[i];
    }

    FFT(double [] timeSeries)
    {
	int blockEnd, blockStart, blockSize;
	nSamples = timeSeries.length;
	real = new double[nSamples];
	imag = new double[nSamples];
	
	doFFT(timeSeries);
    }


    public static void main(String args[])
    {
	double [] test = new double[256];
	for(int i=0;i<256;i++)
	    test[i] = Math.sin((double)i/18.0);

	FFT newFFT = new FFT(test);
	System.out.println("Fundamental = " + newFFT.getDominant(4, 16, 256.0));

    }


}
