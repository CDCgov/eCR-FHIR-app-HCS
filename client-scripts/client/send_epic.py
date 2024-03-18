from datetime import datetime
from time import sleep
import requests
import uuid
import json

#status_url = "http://52.1.183.198:8081/api/submitted-status?"
status_url = "http://localhost:8081/api/submitted-status?"

epic_encounter_id = "eooMam3V951eHK.QpHtYZSg3"
epic_patient_id = "e1deT4MLutEFvvrrFZeN5EA3"

cerner_encounter_id = "4699408"
cerner_patient_id = "5125830"

headers = {"Content-Type": "application/json", "X-Request-ID": "100", "Disable-Changedetect" : "true"}
headers["X-Request-ID"] = str(uuid.uuid4())

def is_submit():
    headers["X-Request-ID"] = str(uuid.uuid4())
    cerner_response = requests.get(url=(status_url + 'encounter=' + cerner_encounter_id + '&patient=' + cerner_patient_id), headers=headers)
    cerner_status = json.loads(cerner_response.content)
    cerner_time = datetime.strptime(cerner_status['submit-time'], '%Y-%m-%d %H:%M:%S.%f') if 'submit-time' in cerner_status else None
    #print(cerner_response.url, cerner_response.status_code, cerner_response.content, cerner_response.text)
    headers["X-Request-ID"] = str(uuid.uuid4())
    epic_response = requests.get(url=(status_url + 'encounter=' + epic_encounter_id + '&patient=' + epic_patient_id), headers=headers)    
    epic_status = json.loads(epic_response.content)
    epic_time = datetime.strptime(epic_status['submit-time'], '%Y-%m-%d %H:%M:%S.%f') if 'submit-time' in epic_status else None
    #print(epic_response.url, epic_response.status_code, epic_response.content, epic_response.text)
    if (epic_response.status_code != 200):    
        print("submit epic") 
        return True
    elif (epic_response.status_code == 200 and cerner_response.status_code == 200 and epic_time < cerner_time):    
        print("submit epic") 
        return True
    else:        
        return False

#event_url = "http://52.1.183.198:8081/api/receive-notification"
event_url = "http://localhost:8081/api/receive-notification"
# json file for event notification
f = open("./epic-p-e1deT4MLutEFvvrrFZeN5EA3-e-eooMam3V951eHK.QpHtYZSg3.json")
json_obj = json.load(f)

while True:
    if (is_submit()):
        headers["X-Request-ID"] = str(uuid.uuid4())
        response = requests.post(event_url, json=json_obj, headers=headers)
        print(headers["X-Request-ID"], response.status_code, response.content, f.name)
    else:
        print("NO SUBMIT")
    sleep(60)    

