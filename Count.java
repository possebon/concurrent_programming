/* Copyright (C) 2006 M. Ben-Ari. See copyright.txt */

class Count extends Thread {
    static volatile int n = 0;

    public void run() {
      int temp;
      
            
      for (int i = 0; i < 10000; i++) {
        temp = n;
        n = temp + 1;
      }

    }

    public static void main(String[] args) {
      Count p = new Count();
      Count q = new Count();
      long before = System.nanoTime();

      p.start();
      q.start();
      try { p.join(); q.join(); }
      catch (InterruptedException e) { }
      long after = System.nanoTime();
      long time = after - before;
      System.out.println("The value of n is " + n);
      System.out.println("The total time of execution is " + time + " nanoseconds");
    }
	
    // o que se pode esperar do valor de n ao final da execução
    // qual o problema?
	
}