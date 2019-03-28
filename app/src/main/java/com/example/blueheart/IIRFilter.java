package com.example.blueheart;

// Implementation of an Infinite Impulse Response (IIR) filter
// with recurrence equation:
//   y[n] = -\sum_{i=1}^M a_i y[n-i] + \sum_{i=0}^N b_i x[n-i]
public class IIRFilter {

    public IIRFilter(float a_[], float b_[]) {
        // initialize memory elements
        int N = Math.max(a_.length, b_.length);
        memory = new float[N-1];
        for (int i = 0; i < memory.length; i++) {
            memory[i] = 0.0f;
        }
        // copy filter coefficients
        a = new float[N];
        int i = 0;
        for (; i < a_.length; i++) {
            a[i] = a_[i];
        }
        for (; i < N; i++) {
            a[i] = 0.0f;
        }
        b = new float[N];
        i = 0;
        for (; i < b_.length; i++) {
            b[i] = b_[i];
        }
        for (; i < N; i++) {
            b[i] = 0.0f;
        }
    }

    // Filter samples from input buffer, and store result in output buffer.
    // Implementation based on Direct Form II.
    // Works similar to matlab's "output = filter(b,a,input)" command
    public void process(float input[], float output[]) {
        for (int i = 0; i < input.length; i++) {
            float in  = input[i];
            float out = 0.0f;
            for (int j = memory.length-1; j >= 0; j--) {
                in  -= a[j+1] * memory[j];
                out += b[j+1] * memory[j];
            }
            out += b[0] * in;
            output[i] = out;
            // shift memory
            for (int j = memory.length-1; j > 0; j--) {
                memory[j] = memory[j - 1];
            }
            memory[0] = in;
        }
    }

    private float[] a;
    private float[] b;
    private float[] memory;
}