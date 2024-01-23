/**

 *
 *	@author		Nita Cosmin & Negura Lucian
 *	@email		cosmin.nita1@student.usv.ro
  *	@email		lucian.negura@student.usv.ro
 */

/* Includere modul */
#include "stm32f4xx.h"
/* Includere librarii folosite */
#include "defines.h"
#include "tm_stm32f4_delay.h"
#include "tm_stm32f4_adc.h"
#include "tm_stm32f4_usart.h"
#include <stdio.h>

int main(void) {
	char str[150];
	
	/* Initializare sistem */
	SystemInit();
	
	/* Initializare Delay*/
	TM_DELAY_Init();
	
	/* Initializare USART1, 115200 baud, TX: PB6 */
	TM_USART_Init(USART1, TM_USART_PinsPack_2, 115200);
	
	/* Initialize ADC1 */
	TM_ADC_InitADC(ADC1);
	
	/* Activare canal VBAT */
	TM_ADC_EnableVbat();
	
	while (1) {
		/* Citire si afisare date */
		sprintf(str, "----------------------------\n Vbat voltaj: %d mV\n", TM_ADC_ReadVbat(ADC1));
		
		/* Transmitem prin USART */
		TM_USART_Puts(USART1, str);
		
		/* Mic delay :) */
		Delayms(1000);
	}
}
