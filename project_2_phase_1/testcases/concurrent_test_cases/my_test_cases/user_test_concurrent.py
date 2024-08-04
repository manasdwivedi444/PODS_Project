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

def main():
    try:
        response = delete_users()
        if (response.status_code!= 200):
            print("tftytc")
            return False
        
    except Exception as e:
        print(e)
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

def test(thread_id):
    #Test for creating user
    user_response = create_user("abc","abc"+str(thread_id)+"@xyz.com")
    if(user_response.status_code != 201):
        print("failed create user for thread-"+str(thread_id))
        return
    user=user_response.json()
    print("printing created user for thread-"+str(thread_id))
    print(user)
    if(user['name']!="abc" or user['email']!="abc"+str(thread_id)+"@xyz.com"):
        print("incorrect user for thread-"+str(thread_id))
        return
    
    print("passed test for creating user for thread-"+str(thread_id))
    
    #Test for creating user with existing email
    user_response = create_user("xyz","abc"+str(thread_id)+"@xyz.com")
    if(user_response.status_code != 400):
        print("failed to check email for thread-"+str(thread_id))
        return
    
    print("passed test for creating user with existing email for thread-"+str(thread_id))
    
    user_response = create_user("abcd","abcd"+str(thread_id)+"@xyz.com")
    new_user = user_response.json()
    user_id1 = new_user['id']
    user_response = create_user("abcde","abcde"+str(thread_id)+"@xyz.com")
    new_user = user_response.json()
    user_id2 = new_user['id']

    #Test for get user
    user_response = get_user(user_id1)
    user=user_response.json()
    print("printing get user for thread-"+str(thread_id))
    print(user)
    if(user['id']!=user_id1 or user['name']!="abcd" or user['email']!="abcd"+str(thread_id)+"@xyz.com"):
        print("incorrect user for thread-"+str(thread_id))
        return
    
    print("passed test for get user for thread-"+str(thread_id))
    
    #Test for deleting exising user
    user_response = delete_user(user_id1)
    if(user_response.status_code!=200):
        print("unable to delete for thread-"+str(thread_id))
        return
    
    print("passed test for deleting existing user for thread-"+str(thread_id))
    
    #Test for accessing non-existing user
    user_response = get_user(user_id1)
    if(user_response.status_code!=404):
        print("failed access non-existing user for thread-"+str(thread_id))
        return
    
    print("passed test for accessing non-existing user for thread-"+str(thread_id))

    #Test for deleting non-existing user
    user_response = delete_user(user_id1)
    if(user_response.status_code!=404):
        print("failed delete non-existing user for thread-"+str(thread_id))
        return
    
    print("passed test for deleting non-existing user for thread-"+str(thread_id))

if __name__ == "__main__":
    main()
