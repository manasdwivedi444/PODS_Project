import requests

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
    delete_users()
    delete_wallets()

    #creating three users
    user_response=create_user("abc","abc@xyz.com")
    user=user_response.json()
    userid1=user["id"]
    user_response=create_user("abcd","abcd@xyz.com")
    user=user_response.json()
    userid2=user["id"]
    user_response=create_user("abcde","abcde@xyz.com")
    user=user_response.json()
    userid3=user["id"]

    #get non-existing wallet
    wallet_response=get_wallet(userid1)
    if(wallet_response.status_code!=404):
        print("error geting non-existing wallet")
        return
    
    print("passed test for get non-existing wallet")
    
    #delete non-existing wallet
    wallet_response=delete_wallet(userid1)
    if(wallet_response.status_code!=404):
        print("error gdelete non-existing wallet")
        return
    
    print("passed test for deleting non-existing wallet")
    
    #create a wallet
    wallet_response=put_wallet(userid1,"credit",1000)
    print("printing wallet after create")
    print(wallet_response.json())
    if(wallet_response.status_code!=200):
        print("error put wallet")
        return
    
    print("passed test for create a wallet")
    
    #update the wallet
    wallet_response=put_wallet(userid1,"credit",1000)
    wallet=wallet_response.json()
    if(int(wallet["balance"])!=2000):
        print("error updating wallet")
        return

    print("passed test for updating wallet")

    #debit large amount form wallet
    wallet_response=put_wallet(userid1,"debit",10000)
    if(wallet_response.status_code!=400):
        print("error debit large amount")
        return
    
    print("passed test for debiting large amount")
    
    #debit from wallet
    wallet_response=put_wallet(userid1,"debit",500)
    wallet=wallet_response.json()
    if(int(wallet["balance"])!=1500):
        print("error debit wallet")
        return
    
    print("passed test for debiting from wallet")

    #get wallet of existing user
    wallet_response=get_wallet(userid1)
    print("printing wallet")
    print(wallet_response.json())
    if(wallet_response.status_code!=200):
        print("error geting existing wallet")
        return
    
    print("passed test for getting wallet for existing wallet")
    
    #delete wallet of user
    wallet_response=delete_wallet(userid1)
    if(wallet_response.status_code!=200):
        print("error deleting wallet")
        return
    
    print("passed test for deleting wallet")
    
    #getting deleted wallet
    wallet_response=get_wallet(userid1)
    if(wallet_response.status_code!=404):
        print("error geting deleted wallet")
        return
    
    print("passed test for getting deleted wallet")
    
    #delete user and get its wallet
    wallet_response=put_wallet(userid1,"credit",1000)
    user_response=delete_user(userid1)
    wallet_response=get_wallet(userid2)
    if(wallet_response.status_code!=404):
        print("error geting deleted user wallet")
        return
    
    print("passed test for deleting user and get its wallet")

    #delete all wallets and retrive it
    wallet_response=put_wallet(userid1,"credit",1000)
    wallet_response=put_wallet(userid1,"credit",1000)
    wallet_response=delete_wallets()
    if(wallet_response.status_code!=200):
       print("error deleting all wallets")
       return
    wallet_response=get_wallet(userid1)
    if(wallet_response.status_code!=404):
        print("error geting deleted wallet")
        return
    
    print("passed test for deleting all wallets")

if __name__ == "__main__":
    main()