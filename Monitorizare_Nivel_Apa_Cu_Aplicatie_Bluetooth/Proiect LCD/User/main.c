/**
 *  Defines for your entire project at one place
 * 
 *	@author 	Balan Victor & Bradatan Cosmin
 *	@email		victor.balan@student.usv.ro
 *  @email		cosmin.bradatan@student.usv.ro
 *	@version 	v1.0
 *	@ide		Keil uVision 5
 *	
 */

/* Includere modul */
#include "stm32f4xx.h"
#include "defines.h"
#include "tm_stm32f4_delay.h"
#include "tm_stm32f4_usart.h"
#include "tm_stm32f4_adc.h"
#include <stdio.h>

/* Function prototypes */
float ConvertToDistance(int adcValue);

int main(void) {
    char str[10];
	  char str2[30];
    int adcValue;
    float distance;

    /* Initialize system */
    SystemInit();

    /* Initialize Delay */
    TM_DELAY_Init();

    /* Initialize USART3, 9600 baud, PinsPack 1 for HC-05*/
    TM_USART_Init(USART3, TM_USART_PinsPack_1, 9600);

	  /* Initialize USART1, 115200 baud, PinsPack 1 for terra term*/
    TM_USART_Init(USART1, TM_USART_PinsPack_1, 115200);
	
    /* Initialize ADC1 on channel 6 (PA6) */
    TM_ADC_Init(ADC1, ADC_Channel_6);

    /* Wait for connection */
    while (TM_USART_BufferEmpty(USART3)) {
        Delayms(100);
    }

    while (1) {
        /* Read ADC1 channel 6 */
        adcValue = TM_ADC_Read(ADC1, ADC_Channel_6);

        /* Convert ADC value to distance in cm(water level in cm) */
        distance = ConvertToDistance(adcValue);

        /* Format and send the data over USART to HC-05 bluetooth module*/
        sprintf(str, "%.2f\n\r", distance);
        TM_USART_Puts(USART3, str);
			
				/* Format and send the data over USART to serial console*/
				sprintf(str2, "Current level of water %.2f cm\n\r", distance);
				TM_USART_Puts(USART1,str2);

        /* Small delay */
        Delayms(100);
    }
}

/* Converts ADC value to distance/water level (cm) */
float ConvertToDistance(int adcValue) {
    const int ADC_MAX = 1023; // Max ADC value for 10-bit ADC
    const float DISTANCE_MIN = 0.0; // Minimum distance
    const float DISTANCE_MAX = 5.0; // Maximum distance

    return ((float)adcValue / ADC_MAX) * (DISTANCE_MAX - DISTANCE_MIN) + DISTANCE_MIN;
}

