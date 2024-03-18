import requests
import json
import time
import uuid


# json file for event notification
# Cerner patient Khalid, Akeem
f = open("./cerner-p5125830-e4699408.json")
json_obj = json.load(f)

# EPIC patient
f2 = open("./epic-p-e1deT4MLutEFvvrrFZeN5EA3-e-eooMam3V951eHK.QpHtYZSg3.json")
json_obj2 = json.load(f2)



headers = {"Content-Type": "application/json", "X-Request-ID": "100"}
#print(headers)

#event_url = "http://52.1.183.198:8081/api/receive-notification"
event_url = "http://localhost:8081/api/receive-notification"
while True:
    headers["X-Request-ID"] = str(uuid.uuid4())
    #print(headers)
    response = requests.post(event_url, json=json_obj, headers=headers)
    print(headers["X-Request-ID"], response.status_code, response.content, f.name)

    time.sleep(10)
    headers["X-Request-ID"] = str(uuid.uuid4())
    response = requests.post(event_url, json=json_obj2, headers=headers)
    print(headers["X-Request-ID"], response.status_code, response.content, f2.name)
    
    #print("reqId=" + headers.get("X-Request-ID") + ", status=" + str(response.status_code) + ", content=" + str(response.content))
    #print(headers.get("X-Request-ID"), str(response.status_code), str(response.content))
    time.sleep(300)
