from __future__ import print_function
import sys
import ssl
import time
import datetime
import logging, traceback
import paho.mqtt.client as mqtt

IoT_protocol_name = "x-amzn-mqtt-ca"
aws_iot_endpoint = "a2zjnxhx7cmhf4-ats.iot.us-east-2.amazonaws.com" # <random>.iot.<region>.amazonaws.com
url = "https://{}".format(aws_iot_endpoint)

ca =      "../VeriSign-Class 3-Public-Primary-Certification-Authority-G5.pem.txt" #"JavaInatelTCC/VeriSign-Class 3-Public-Primary-Certification-Authority-G5.pem.crt" 
cert =    "../945966a60a-certificate.pem.crt" #"JavaInatelTCC/MallCAM1.cert.pem"
private = "../945966a60a-private.pem.key"     #"JavaInatelTCC/MallCAM1.private.pem.key"

logger = logging.getLogger()
logger.setLevel(logging.DEBUG)
handler = logging.StreamHandler(sys.stdout)
log_format = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
handler.setFormatter(log_format)
logger.addHandler(handler)

def on_message(client, userdata, msg):
    print("   [" +msg.topic+"] =>"+str(msg.payload))


def ssl_alpn():
    try:
        #debug print opnessl version
        logger.info("open ssl version:{}".format(ssl.OPENSSL_VERSION))
        ssl_context = ssl.create_default_context()
        ssl_context.set_alpn_protocols([IoT_protocol_name])
        ssl_context.load_verify_locations(cafile=ca)
        ssl_context.load_cert_chain(certfile=cert, keyfile=private)

        return  ssl_context
    except Exception as e:
        print("exception ssl_alpn()")
        raise e

if __name__ == '__main__':
    topic = "bla" #"$aws/things/Dobro1/shadow/update"
    try:
        mqttc = mqtt.Client()
        mqttc.on_message = on_message
        
        ssl_context= ssl_alpn()
        mqttc.tls_set_context(context=ssl_context)
        logger.info("start connect")
        mqttc.connect(aws_iot_endpoint, port=443)
        logger.info("connect success")
        
        try:
            ret = mqttc.subscribe("#")
            logger.info("Try to subscribe to =>")
            logger.info(ret)
            
        except Exception as e:
            logger.error("exception main()")
            logger.error("e obj:{}".format(vars(e)))
            logger.error("message:{}".format(e.message))
            traceback.print_exc(file=sys.stdout)
            
        mqttc.loop_start()

        while True:
            now = datetime.datetime.now().strftime('%Y-%m-%dT%H:%M:%S')
            logger.info("try to publish: [" +topic  +"] {}".format(now))
            ret = mqttc.publish(topic, "{ \"horaAtual\":\"" +now +"\"}")
            logger.info(ret)
            time.sleep(10)
            
            #mqttc.loop_forever()
# 2019-03-09 21:28:05,622 - root - INFO - (4, 30)
# => The value of rc indicates success or not:
#  0: Connection successful 1: Connection refused - incorrect protocol version  2: Connection refused - invalid client identifier 
# 3: Connection refused - server unavailable 4: Connection refused - bad username or password 5: Connection refused - not authorised 
# 6-255: Currently unused.

    except Exception as e:
        logger.error("exception main()")
        logger.error("e obj:{}".format(vars(e)))
        logger.error("message:{}".format(e.message))
        traceback.print_exc(file=sys.stdout)