import requests

userServiceURL = "http://localhost:8080"
bookingServiceURL = "http://localhost:8081"
walletServiceURL = "http://localhost:8082"

def delete_users():
    requests.delete(userServiceURL+f"/users") 

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
    delete_users()
    #Test for creating user
    user_response = create_user("abc","abc@xyz.com")
    if(user_response.status_code != 201):
        print("failed create user")
        return
    user=user_response.json()
    print("printing created user")
    print(user)
    if(user['name']!="abc" or user['email']!="abc@xyz.com"):
        print("incorrect user")
        return
    
    print("passed test for creating user")
    
    #Test for creating user with existing email
    user_response = create_user("xyz","abc@xyz.com")
    if(user_response.status_code != 400):
        print("failed to check email")
        return
    
    print("passed test for creating user with existing email")
    
    user_response = create_user("abcd","abcd@xyz.com")
    new_user = user_response.json()
    user_id1 = new_user['id']
    user_response = create_user("abcde","abcde@xyz.com")
    new_user = user_response.json()
    user_id2 = new_user['id']

    #Test for get user
    user_response = get_user(user_id1)
    user=user_response.json()
    print("printing get user")
    print(user)
    if(user['id']!=user_id1 or user['name']!="abcd" or user['email']!="abcd@xyz.com"):
        print("incorrect user")
        return
    
    print("passed test for get user")
    
    #Test for deleting exising user
    user_response = delete_user(user_id1)
    if(user_response.status_code!=200):
        print("unable to delete")
        return
    
    print("passed test for deleting existing user")
    
    #Test for accessing non-existing user
    user_response = get_user(user_id1)
    if(user_response.status_code!=404):
        print("failed access non-existing user")
        return
    
    print("passed test for accessing non-existing user")

    #Test for deleting non-existing user
    user_response = delete_user(user_id1)
    if(user_response.status_code!=404):
        print("failed delete non-existing user")
        return
    
    print("passed test for deleting non-existing user")
    
    #Test for deleting all users
    user_response = delete_users()
    user_response = get_user(user_id2)
    if(user_response.status_code!=404):
        print("failed access non-existing user")
        return
    
    print("passes test for deleting all user")

if __name__ == "__main__":
    main()
