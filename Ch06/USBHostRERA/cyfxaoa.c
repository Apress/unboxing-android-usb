/*
 ## Cypress USB 3.0 Platform source file (cyfxmousedrv.c)
 ## ===========================
 ##
 ##  Copyright Cypress Semiconductor Corporation, 2010-2011,
 ##  All Rights Reserved
 ##  UNPUBLISHED, LICENSED SOFTWARE.
 ##
 ##  CONFIDENTIAL AND PROPRIETARY INFORMATION
 ##  WHICH IS THE PROPERTY OF CYPRESS.
 ##
 ##  Use of this file is governed
 ##  by the license agreement included in the file
 ##
 ##     <install>/license/license.txt
 ##
 ##  where <install> is the Cypress software
 ##  installation root directory path.
 ##
 ## ===========================
*/

/* This file illustrates the mouse driver. */

/*
   A simple single interface USB HID mouse will be successfully enumerated
   and the current offset will be printed via the UART debug logs.

   We support only single interface with interface class = HID(0x03),
   interface sub class = Boot (0x01) and interface protocol = Mouse (0x02).
   This example supports only 4 byte input reports with the following format:
        BYTE0: Bitmask for each of the button present.
        BYTE1: Signed movement in X direction.
        BYTE2: Signed movement in Y direction.
        BYTE3: Signed movement in scroll wheel.
   Further types can be implemented by decoding the HID descriptor.
*/

#include "cyu3system.h"
#include "cyu3os.h"
#include "cyu3dma.h"
#include "cyu3error.h"
#include "cyu3usb.h"
#include "cyu3usbhost.h"
#include "cyu3usbotg.h"
#include "cyu3utils.h"
#include "cyfxusbhost.h"


void switch_to_acc();

#define USB_SETUP_HOST_TO_DEVICE                0x00    // Device Request bmRequestType transfer direction - host to device transfer
#define USB_SETUP_DEVICE_TO_HOST                0x80    // Device Request bmRequestType transfer direction - device to host transfer
#define USB_SETUP_TYPE_STANDARD                 0x00    // Device Request bmRequestType type - standard
#define USB_SETUP_TYPE_CLASS                    0x20    // Device Request bmRequestType type - class
#define USB_SETUP_TYPE_VENDOR                   0x40    // Device Request bmRequestType type - vendor
#define USB_SETUP_RECIPIENT_DEVICE              0x00    // Device Request bmRequestType recipient - device
#define USB_SETUP_RECIPIENT_INTERFACE           0x01    // Device Request bmRequestType recipient - interface
#define USB_SETUP_RECIPIENT_ENDPOINT            0x02    // Device Request bmRequestType recipient - endpoint
#define USB_SETUP_RECIPIENT_OTHER               0x03    // Device Request bmRequestType recipient - other


#define ACCESSORY_STRING_MANUFACTURER   0
#define ACCESSORY_STRING_MODEL          1
#define ACCESSORY_STRING_DESCRIPTION    2
#define ACCESSORY_STRING_VERSION        3
#define ACCESSORY_STRING_URI            4
#define ACCESSORY_STRING_SERIAL         5

#define ACCESSORY_GET_PROTOCOL          51
#define ACCESSORY_SEND_STRING           52
#define ACCESSORY_START                 53

//extern uint8_t glEp0Buffer[];

/* DMA callback for the mouse application. */
void
CyFxAoADmaCb (CyU3PDmaChannel *ch,
        CyU3PDmaCbType_t type,
        CyU3PDmaCBInput_t *input)
{
	uint16_t i;
	uint16_t count = input->buffer_p.count;
	uint8_t * buf = input->buffer_p.buffer;

    if (type == CY_U3P_DMA_CB_PROD_EVENT)
    {
//    	CyU3PDmaChannelDiscardBuffer(ch);
//    	CyU3PDebugPrint(2, "Got data\n");
//    	return;

//    	gotData = CyTrue;

        prodCount++;
        CyU3PEventSet(&glDMAEvent, PROD_EVENT, CYU3P_EVENT_OR);
    } else if (type == CY_U3P_DMA_CB_CONS_EVENT) {
    	consCount++;
    }
}

/* Initalizes the mouse driver. */
CyU3PReturnStatus_t
CyFxAOADriverInit ()
{
    uint16_t length, size, interval;
    CyU3PReturnStatus_t status;
    CyU3PUsbHostEpConfig_t epCfg;
    CyU3PDmaChannelConfig_t dmaCfg;

    CyU3PDebugPrint (4, "In CyFxAOADriverInit start\r\n");

#if 0
   /*  Read first four bytes of configuration descriptor to determine
      the total length.*/
    status = CyFxSendSetupRqt (0x80, CY_U3P_USB_SC_GET_DESCRIPTOR,
            (CY_U3P_USB_CONFIG_DESCR << 8), 0, 4, glEp0Buffer);
    if (status != CY_U3P_SUCCESS)
    {
        goto enum_error;
    }

    /* Identify the length of the data received.*/
    length = CY_U3P_MAKEWORD(glEp0Buffer[3], glEp0Buffer[2]);
    if (length > CY_FX_HOST_EP0_BUFFER_SIZE)
    {
        goto enum_error;
    }

    /* Read the full configuration descriptor.*/
    status = CyFxSendSetupRqt (0x80, CY_U3P_USB_SC_GET_DESCRIPTOR,
            (CY_U3P_USB_CONFIG_DESCR << 8), 0, length, glEp0Buffer);
    if (status != CY_U3P_SUCCESS)
    {
        goto enum_error;
    }
#endif
    /* Set the new configuration. */
       status = CyFxSendSetupRqt (0x00, CY_U3P_USB_SC_SET_CONFIGURATION, 1, 0, 0, glEp0Buffer);
       if (status != CY_U3P_SUCCESS)
       {
           goto enum_error;
       }
       CyU3PDebugPrint (4, "Set Config success\r\n");

//		/* Set the interface. */
//		status = CyFxSendSetupRqt (0x01, CY_U3P_USB_SC_SET_INTERFACE, 0, 2, 0, glEp0Buffer);
//		if (status != CY_U3P_SUCCESS)
//		{
//			goto enum_error;
//		}
//		CyU3PDebugPrint (4, "Set interface success\r\n");

		/* Set the interface. */
		status = CyFxSendSetupRqt (0x01, CY_U3P_USB_SC_SET_INTERFACE, 1, 2, 0, glEp0Buffer);
		if (status != CY_U3P_SUCCESS)
		{
			goto enum_error;
		}
		CyU3PDebugPrint (4, "Set interface success\r\n");

	    CyU3PThreadSleep (5);

		glEp0Buffer[0] = 0x44;
		glEp0Buffer[1] = 0xAC;
		glEp0Buffer[2] = 0;
		/* Set the set_cur. */
		status = CyFxSendSetupRqt (0x22, 1, 0x0100, 0x83, 3, glEp0Buffer);
		if (status != CY_U3P_SUCCESS)
		{
			CyU3PDebugPrint (4, "Set CUR failed\r\n");
			//goto enum_error;
		}
		else
			CyU3PDebugPrint (4, "Set CUR success\r\n");

		/* Set the get_cur. */
		status = CyFxSendSetupRqt (0xA2, 0x81, 0x0100, 0x83, 3, glEp0Buffer);
		if (status != CY_U3P_SUCCESS)
		{
			CyU3PDebugPrint (4, "Get CUR failed\r\n");
			//goto enum_error;
		}
		else
			CyU3PDebugPrint (4, "Get CUR success\r\n");


       /* Add the IN endpoint. */
       CyU3PMemSet ((uint8_t *)&epCfg, 0, sizeof(epCfg));
       epCfg.type = CY_U3P_USB_EP_ISO;
       epCfg.mult = 1;
       epCfg.maxPktSize = 256;
       epCfg.pollingRate = 1;
       /* Since DMA buffer sizes can only be multiple of 16 bytes and
        * also since this is an interrupt endpoint where the max data
        * packet size is same as the maxPktSize field, the fullPktSize
        * has to be a multiple of 16 bytes. */
       size = ((256 + 0x0F) & ~0x0F);
       epCfg.fullPktSize = size;
       epCfg.isStreamMode = CyTrue;
       status = CyU3PUsbHostEpAdd (0x83, &epCfg);
       if (status != CY_U3P_SUCCESS)
       {
           goto enum_error;
       }
       CyU3PDebugPrint (4, "EP Config success\r\n");

       /* Create a DMA channel for this EP. */
       CyU3PMemSet ((uint8_t *)&dmaCfg, 0, sizeof(dmaCfg));
       dmaCfg.size = size;
       dmaCfg.count = 6;
       dmaCfg.prodSckId = (CyU3PDmaSocketId_t)(CY_U3P_UIB_SOCKET_PROD_0 + (0x0F & 0x83));
       dmaCfg.consSckId = CY_U3P_CPU_SOCKET_CONS;
       dmaCfg.dmaMode = CY_U3P_DMA_MODE_BYTE;
       dmaCfg.notification = CY_U3P_DMA_CB_PROD_EVENT;
       dmaCfg.cb = CyFxAoADmaCb;
       dmaCfg.prodHeader = 0;
       dmaCfg.prodFooter = 0;
       dmaCfg.consHeader = 0;
       dmaCfg.prodAvailCount = 0;
       status = CyU3PDmaChannelCreate (&glHostAoACh, CY_U3P_DMA_TYPE_MANUAL_IN, &dmaCfg);
       if (status != CY_U3P_SUCCESS)
       {
           goto app_error;
       }

       dmaCfg.prodSckId = CY_U3P_CPU_SOCKET_PROD;
       dmaCfg.consSckId = CY_U3P_LPP_SOCKET_I2S_LEFT;
       dmaCfg.notification = 0;
       dmaCfg.cb = NULL;
       status = CyU3PDmaChannelCreate (&glI2SLeftCh, CY_U3P_DMA_TYPE_MANUAL_OUT, &dmaCfg);
       if (status != CY_U3P_SUCCESS)
       {
           goto app_error;
       }

       dmaCfg.prodSckId = CY_U3P_CPU_SOCKET_PROD;
       dmaCfg.consSckId = CY_U3P_LPP_SOCKET_I2S_RIGHT;
       status = CyU3PDmaChannelCreate (&glI2SRightCh, CY_U3P_DMA_TYPE_MANUAL_OUT, &dmaCfg);
       if (status != CY_U3P_SUCCESS)
       {
           goto app_error;
       }

       CyU3PDebugPrint (4, "DMA Config success\r\n");

       status = CyU3PDmaChannelSetXfer (&glI2SLeftCh, 0);
       if (status != CY_U3P_SUCCESS)
       {
           goto app_error;
       }
       CyU3PDebugPrint (4, "left out success\r\n");

       status = CyU3PDmaChannelSetXfer (&glI2SRightCh, 0);
       if (status != CY_U3P_SUCCESS)
       {
    	   CyU3PDebugPrint (4, "right out failed 0x%x\r\n", status);
           goto app_error;
       }
       CyU3PDebugPrint (4, "right out success\r\n");

       /* Enable EP transfer. In stream mode, the transfer size should be zero. */
	   status = CyU3PUsbHostEpSetXfer (0x83, CY_U3P_USB_HOST_EPXFER_NORMAL, 0);
	   if (status != CY_U3P_SUCCESS)
	   {
	 	  goto app_error;
	   }
	   CyU3PDebugPrint (4, "hostepsetxfer success\r\n");

       /* Set for infinite transfer. */
       status = CyU3PDmaChannelSetXfer (&glHostAoACh, 0);
       if (status != CY_U3P_SUCCESS)
       {
           goto app_error;
       }
       CyU3PDebugPrint (4, "in dma setxfer success\r\n");

       CyU3PDebugPrint (4, "In CyFxAOADriverInit end\r\n");


       return CY_U3P_SUCCESS;

app_error:
   CyU3PDmaChannelDestroy (&glHostAoACh);
   CyU3PUsbHostEpRemove (0x83);

enum_error:
    return CY_U3P_ERROR_FAILURE;
}

/* Disables the mouse driver. */
void
CyFxAOADriverDeInit ()
{

}


int getProtocol()
{
    int protocol = -1;
    CyFxSendSetupRqt( USB_SETUP_DEVICE_TO_HOST |
                USB_SETUP_TYPE_VENDOR |
                USB_SETUP_RECIPIENT_DEVICE,
                ACCESSORY_GET_PROTOCOL,0,0,2,glEp0Buffer);
    protocol = glEp0Buffer[0];
    return protocol;
}

void sendString(int index, const char *str)
{
	CyU3PDebugPrint (4, "In AOA Send String\r\n");

    CyFxSendSetupRqt(USB_SETUP_HOST_TO_DEVICE |
                USB_SETUP_TYPE_VENDOR |
                USB_SETUP_RECIPIENT_DEVICE,
                ACCESSORY_SEND_STRING, 0,index,
                 strlen(str) + 1,(char *)str);
}

void switch_to_acc()
{
	int status = 0;

    CyU3PDebugPrint (4, "Switch to AOA protocol\r\n");

    CyU3PThreadSleep (100);
    CyU3PThreadSleep (100);


    int aoa_proto_ver = getProtocol();

    CyU3PDebugPrint (4, "In AOA protocol version\r\n");

    if(aoa_proto_ver < 2)
    {
        //printf("Less than 2");
    }
    else if(aoa_proto_ver == 2)
    {
        //printf("Equals 2");
    }
    else
    {
       // printf("Other than 1 or 2");
    }
    sendString( ACCESSORY_STRING_MANUFACTURER, "Cypress");

    sendString(ACCESSORY_STRING_MODEL, "FX3 NFC Reader");

    sendString( ACCESSORY_STRING_DESCRIPTION, "FX3 Android Accessory NFC Reader");

    sendString(ACCESSORY_STRING_VERSION, "0.1");

    sendString(ACCESSORY_STRING_URI, "www.cypress.com");

    sendString(ACCESSORY_STRING_SERIAL, "0123456789");

    status = CyFxSendSetupRqt(
                USB_SETUP_HOST_TO_DEVICE |
                USB_SETUP_TYPE_VENDOR |
                USB_SETUP_RECIPIENT_DEVICE,
                58, 1,0,
                 0,0);


    status = CyFxSendSetupRqt(
                USB_SETUP_HOST_TO_DEVICE |
                USB_SETUP_TYPE_VENDOR |
                USB_SETUP_RECIPIENT_DEVICE,
                ACCESSORY_START, 0,0,
                 0,0);

    CyU3PDebugPrint (4, "AOA START %d\r\n",status);

    CyU3PThreadSleep (100);
    CyU3PThreadSleep (100);
    CyU3PThreadSleep (100);

}
/* [ ] */

