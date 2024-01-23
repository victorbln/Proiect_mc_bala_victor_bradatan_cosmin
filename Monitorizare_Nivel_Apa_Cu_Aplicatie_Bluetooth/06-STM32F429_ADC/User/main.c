/**

 *
 *	@author		Nita Cosmin & Negura Lucian
 *	@email		cosmin.nita1@student.usv.ro
  *	@email		lucian.negura@student.usv.ro

 *
 */

/* Includere modul */
#include "stm32f4xx.h"
/* Includere librarii folosite */
#include "defines.h"
#include "tm_stm32f4_delay.h"
#include "tm_stm32f4_usart.h"
#include "tm_stm32f4_adc.h"
#include <stdio.h>

int main(void) {
	char str[15];
	
	/* Initializare sistem */
	SystemInit();
	
	/* Initializare Delay*/
	TM_DELAY_Init();
	
	/* Initializare USART1, 115200 baud, TX: PB6 */
	TM_USART_Init(USART1, TM_USART_PinsPack_2, 115200);
	
		/* Initialize ADC1 pe canalul 0*/
	TM_ADC_Init(ADC1, ADC_Channel_0);
	
		/* Initialize ADC1 pe canalul #*/
	TM_ADC_Init(ADC1, ADC_Channel_3);
	
	while (1) {
		/* 							Citim ADC1 canalul 0				Citim ADC1 canalul 3 */
		sprintf(str, "%4d: %4d\n\r", TM_ADC_Read(ADC1, ADC_Channel_0), TM_ADC_Read(ADC1, ADC_Channel_3));
		
		/* Transmitem prin USART */
		TM_USART_Puts(USART1, str);
		
		/* Mic delay :) */
		Delayms(100);
	}
}
