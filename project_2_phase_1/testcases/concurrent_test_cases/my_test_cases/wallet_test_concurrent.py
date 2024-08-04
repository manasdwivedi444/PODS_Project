import requests
from threading import Thread

userServiceURL = "http://localhost:8080"
bookingServiceURL = "http://localhost:8081"
walletServiceURL = "http://localhost:8082"

def delete_users():
    response = requests.delete(userServiceURL+f"/users") 
    return response

def create_user(name, email):
    new_user = {"name": name, "email": email}
    response = requests.post(userServiceURL + "/users", json=new_user)
    return response

def get_user(userId):
    response = requests.get(userServiceURL + f"/users/{userId}")
    return response

def delete_user(userId):
    response = requests.delete(userServiceURL + f"/users/{userId}")
    return response

def get_wallet(userId):
    response = requests.get(walletServiceURL + f"/wallets/{userId}")
    return response

def delete_wallets():
    response = requests.delete(walletServiceURL+"/wallets")
    return response    

def put_wallet(userId,action,amount):
    body = {"action": action, "amount": amount}
    response = requests.put(walletServiceURL + f"/wallets/{userId}", json=body)
    return response

def delete_wallet(userId):
    response = requests.delete(walletServiceURL+f"/wallets/{userId}")
    return response

def main():
    try:
        response = delete_users()
        if (response.status_code!= 200):
            return False
    
        response = delete_wallets()
        if (response.status_code!= 200):
            return False
    except:
        return False
    
    thread1 = Thread(target=test, kwargs = {'thread_id': 1})
    thread2 = Thread(target=test, kwargs = {'thread_id': 2})
    thread3 = Thread(target=test, kwargs = {'thread_id': 3})

    thread1.start()
    thread2.start()
    thread3.start()

    thread1.join()
    thread2.join()
    thread3.join()

    wallet_response=delete_wallets()
    if(wallet_response.status_code!=200):
       print("error deleting all wallets")
       return
    print("passed test for deleting user wallet")

def test(thread_id):
    delete_users()
    delete_wallets()

    #creating three users
    user_response=create_user("abc","abc"+str(thread_id)+"@xyz.com")
    user=user_response.json()
    userid1=user["id"]
    user_response=create_user("abcd","abcd"+str(thread_id)+"@xyz.com")
    user=user_response.json()
    userid2=user["id"]
    user_response=create_user("abcde","abcde"+str(thread_id)+"@xyz.com")
    user=user_response.json()
    userid3=user["id"]

    #get non-existing wallet
    wallet_response=get_wallet(userid1)
    if(wallet_response.status_code!=404):
        print("error geting non-existing wallet for thread-"+str(thread_id))
        return
    
    print("passed test for get non-existing wallet for thread-"+str(thread_id))
    
    #delete non-existing wallet
    wallet_response=delete_wallet(userid1)
    if(wallet_response.status_code!=404):
        print("error gdelete non-existing wallet for thread-"+str(thread_id))
        return
    
    print("passed test for deleting non-existing wallet for thread-"+str(thread_id))
    
    #create a wallet
    wallet_response=put_wallet(userid1,"credit",1000)
    print("printing wallet after create")
    print(wallet_response.json())
    if(wallet_response.status_code!=200):
        print("error put wallet")
        return
    
    print("passed test for create a wallet for thread-"+str(thread_id))
    
    #update the wallet
    wallet_response=put_wallet(userid1,"credit",1000)
    wallet=wallet_response.json()
    if(int(wallet["balance"])!=2000):
        print("error updating wallet for thread-"+str(thread_id))
        return

    print("passed test for updating wallet for thread-"+str(thread_id))

    #debit large amount form wallet
    wallet_response=put_wallet(userid1,"debit",10000)
    if(wallet_response.status_code!=400):
        print("error debit large amount for thread-"+str(thread_id))
        return
    
    print("passed test for debiting large amount for thread-"+str(thread_id))
    
    #debit from wallet
    wallet_response=put_wallet(userid1,"debit",500)
    wallet=wallet_response.json()
    if(int(wallet["balance"])!=1500):
        print("error debit wallet for thread-"+str(thread_id))
        return
    
    print("passed test for debiting from wallet for thread-"+str(thread_id))

    #get wallet of existing user
    wallet_response=get_wallet(userid1)
    print("printing wallet for thread-"+str(thread_id))
    print(wallet_response.json())
    if(wallet_response.status_code!=200):
        print("error geting existing wallet for thread-"+str(thread_id))
        return
    
    print("passed test for getting wallet for existing wallet for thread-"+str(thread_id))
    
    #delete wallet of user
    wallet_response=delete_wallet(userid1)
    if(wallet_response.status_code!=200):
        print("error deleting wallet for thread-"+str(thread_id))
        return
    
    print("passed test for deleting wallet for thread-"+str(thread_id))
    
    #getting deleted wallet
    wallet_response=get_wallet(userid1)
    if(wallet_response.status_code!=404):
        print("error geting deleted wallet for thread-"+str(thread_id))
        return
    
    print("passed test for getting deleted wallet for thread-"+str(thread_id))
    
    #delete user and get its wallet
    wallet_response=put_wallet(userid1,"credit",1000)
    user_response=delete_user(userid1)
    wallet_response=get_wallet(userid2)
    if(wallet_response.status_code!=404):
        print("error geting deleted user wallet for thread-"+str(thread_id))
        return

if __name__ == "__main__":
    main()