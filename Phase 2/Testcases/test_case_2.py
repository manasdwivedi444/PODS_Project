import requests
import random
import sys
from http import HTTPStatus
from threading import Thread

from user import *
from booking import *
from wallet import *

import threading 
import time
import json
lock = threading.Lock()


ticketsbooked=0
debitedamount=0
showid=random.randint(1,20)
response=requests.get(bookingServiceURL + f"/shows/{showid}")
showres=response.json()
ticketprize=showres['price']
ticketsinitiallyavailable=showres['seats_available']
# Thread : To book tickets
def t1(user_id):
    for i in range(5):
        global debitedamount
        global ticketsbooked
        no_of_tickets=random.randint(1,10)
        lock.acquire()
        response = requests.post(bookingServiceURL + f"/bookings", json={'show_id': showid, 'user_id': user_id, 'seats_booked':no_of_tickets})
        if response.status_code == 200:

            amounttobedebited=ticketprize*no_of_tickets
            debitedamount = debitedamount+amounttobedebited
            ticketsbooked = ticketsbooked+no_of_tickets

        lock.release()

def main():
    try:
        print(showres)
        response = delete_users()
        if not check_response_status_code(response, 200):
            return False
    
        response = delete_wallets()
        if not check_response_status_code(response, 200):
            return False

        response = post_user('Anurag Kumar','ak47@iisc.ac.in')
        if not test_post_user('Anurag Kumar','ak47@iisc.ac.in', response):
            return False
        user = response.json()

        initial_balance = 1000
        response = put_wallet(user['id'], 'credit', initial_balance)
        if not test_put_wallet(user['id'], 'credit', initial_balance, 0, response):
            return False
        
        ### Parallel Execution Begins ###
        thread1 = Thread(target=t1, kwargs = {'user_id': user['id']})
        thread2 = Thread(target=t1, kwargs = {'user_id': user['id']})

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()
        ### Parallel Execution Ends ###

        walletresponse = get_wallet(user['id'])
        if test_get_wallet(user['id'], walletresponse, True, initial_balance - debitedamount):
            print_pass_message("Wallet is consistent")
        
        
        
        
        
        
        return True
    except:
        return False

if __name__ == "__main__":
    if main():
        sys.exit(0)
    else:
        sys.exit(1)
