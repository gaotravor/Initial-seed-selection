We have provided the bytecode files for the four seed programs and included their key code snippets below to illustrate how these seeds help find more inconsistencies in subsequent fuzzing.

The common characteristics of these seed programs include features such as nested loops, array operations, and complex calculations. These features can help trigger relevant optimizations in the JVM, thereby exploring more interesting behaviors.

* jit.FloatingPoint.gen_math.Loops06.Loops06

  ```java
  for(i = 1; i < 19; ++i) {
      for(int j = 1; j < 19; ++j) {
          zz[0] = 0.0;
          for(int k = 1; k < 19; ++k) {
              for(int n = 1; n < 19; ++n) {
                  yy[0] = 0.0;
                  for(int m = 1; m < 19; ++m) {
                      for(int l = 1; l < 19; ++l) {
                          xx[i] = xx[i - 1] + xx[i + 1];
                      }
                  }
              }
          }
      }
  }
  ```

* jit.FloatingPoint.gen_math.Matrix_3d.Matrix_3d

  ```java
  for(i = 0; i < N; ++i) {
      for(j = 0; j < N; ++j) {
          for(k = 0; k < N; ++k) {
              double r1 = (double)i;
              double r2 = Math.sin(r1);
              double r3 = (double)j;
              double r4 = Math.cos(r3);
              double r5 = (double)k;
              double r6 = Math.sqrt(r5);
              xxx[i][j][k] = r6 * (r2 * r2 + r4 * r4);
          }
      }
  }
  for(i = 0; i < N; ++i) {
      for(j = 0; j < N; ++j) {
          for(k = 0; k < N; ++k) {
              yyy[i][j][k] = xxx[k][j][i];
              zzz[i][j][k] = xxx[k][i][j];
          }
      }
  }
  ```

* vm.compiler.jbe.dead.dead10.dead10

  ```java
  int fopt() {
      this.i1 = this.j + 1;
      this.i2 = this.j - 1;
      this.i3 = this.j * 3;
      this.i4 = this.j / 31;
      this.i5 = this.j % 71;
      this.i6 = this.j << 3;
      this.i7 = this.j >> 4;
      this.i8 = this.j >>> 5;
      this.i9 = this.bol ? 7 : 9;
      this.i10 = ~this.j;
      this.i11 = this.j & 3;
      this.i12 = this.j | 4;
      this.i13 = this.j ^ 4;
      int res = this.i1 + this.i2 + this.i3 + this.i4 + this.i5 + this.i6 + this.i7 + this.i8 + this.i9 + this.i10 + this.i11 + this.i12 + this.i13;
      return res;
  }
  ```

* vm.compiler.jbe.subcommon.subcommon03.subcommon03

  ```java
  void mat() {
      for(this.k = 1; this.k < 10; ++this.k) {
          this.n = this.k * 10;
          for(this.m = 0; this.m < 10; ++this.m) {
              this.arr[this.n + this.m] = (float)Math.exp((double)this.arr[this.m]);
              this.arr1[this.k][this.m] = (float)((double)(this.arr[this.m] * 1.0F) / Math.PI);
              this.arr_opt[this.n + this.m] = (float)Math.exp((double)this.arr_opt[this.m]);
              this.arr1_opt[this.k][this.m] = (float)((double)(this.arr_opt[this.m] * 1.0F) / Math.PI);
          }
      }
  }
  
  void un_optimized() {
      this.c = 1.1234568F;
      this.d = 1.010101F;
      this.a = (float)((double)this.c * Math.sqrt((double)this.d * 2.0) / (2.0 * (double)this.d));
      this.b = (float)((double)this.c / Math.sqrt((double)this.d * 2.0) / (2.0 * (double)this.d));
      System.out.print("a=" + this.a + ";  b=" + this.b);
      this.c = this.arr[0] / (this.arr[0] * this.arr[0] + this.arr[1] * this.arr[1]);
      this.d = this.arr[1] * (this.arr[0] * this.arr[0] + this.arr[1] * this.arr[1]);
      System.out.println(";  c=" + this.c + ";  d=" + this.d);
      this.k = 0;
      float t1 = this.arr[this.k];
      float t2 = this.arr[this.k] * 2.0F;
      this.arr[2] = this.a;
      this.arr[3] = this.b;
      this.arr[4] = this.c;
      this.arr[5] = this.d;
      this.arr[8] = this.b / this.c;
      this.arr[9] = this.c - this.a;
      this.c = t2 / t1 * this.b / this.a;
      this.x = (float)((double)this.d * 2.0);
      this.d = t2 / t1 * this.b / this.a;
      this.arr[6] = this.c;
      this.arr[7] = this.d;
  }
  ```

  
