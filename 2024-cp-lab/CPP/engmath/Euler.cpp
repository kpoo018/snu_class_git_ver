#include <iostream>
#include <math.h>

int main() {
    double y = 0, h=0.1;
    double y_e=y, y_ie=y, y_rk=y;
    double k[4];

    

    for(int i=0 ; i/10 < 1 ; i++ ){
        printf("\n\n y_%d = \n", i+1);

        printf("\n Exact value: ");
        y = 2*exp(0.1*(i+1)) - (0.1*(i+1))*(0.1*(i+1))- 2*h*(i+1)-2;
        printf("%lf ",  y);

        printf("\n Euler method: ");
        y_e = y_e + 0.1*( y_e + (0.1*i)*(0.1*i));
        printf("%lf ",  y_e);
        printf("\n err: ");
        printf("%lf ", abs(y - y_e));

        printf("\n improved Euler method: ");
        double ys = y_ie + (0.1)*(y_ie+(0.1*i)*(0.1*i));
        y_ie = y_ie + 0.5*0.1*( y_ie + (0.1*i)*(0.1*i)+ ys + (0.1*(i+1))*(0.1*(i+1)) );
        printf("%lf ",  y_ie); 
        printf("\n err: ");
        printf("%lf ", abs(y - y_ie));

        printf("\n Runge_Kutta method: ");
        k[0]=h*(y_rk+h*i*h*i);
        k[1]=h*(y_rk+0.5*k[0] + (h*i+0.5*h)*(h*i+0.5*h));
        k[2]=h*(y_rk+0.5*k[1] + (h*i+0.5*h)*(h*i+0.5*h));
        k[3]=h*(y_rk+k[2] + (h*i+h)*(h*i+h));;
        y_rk = y_rk + (1.0/6.0)*(k[0]+2*k[1]+2*k[2]+k[3]);
        printf("%lf ", y_rk); 
        printf("\n err: ");
        printf("%lf ", abs(y - y_rk));

    }

    return 0;
}