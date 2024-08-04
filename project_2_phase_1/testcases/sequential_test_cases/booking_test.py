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

def delete_wallets():
    response = requests.delete(walletServiceURL+"/wallets")
    return response    

def put_wallet(userId,action,amount):
    body = {"action": action, "amount": amount}
    response = requests.put(walletServiceURL + f"/wallets/{userId}", json=body)
    return response

def get_wallet(userId):
    response = requests.get(walletServiceURL + f"/wallets/{userId}")
    return response

def get_theatres():
    response = requests.get(bookingServiceURL+"/theatres")
    return response

def get_shows_with_theatreid(theatreId):
    response = requests.get(bookingServiceURL+f"/shows/theatres/{theatreId}")
    return response

def get_shows_with_showid(showId):
    response = requests.get(bookingServiceURL+f"/shows/{showId}")
    return response

def get_bookings_with_userid(userId):
    response = requests.get(bookingServiceURL+f"/bookings/users/{userId}")
    return response

def create_booking(userId,showId,seatsBooked):
    body = {"show_id":showId,"user_id":userId,"seats_booked":seatsBooked}
    response = requests.post(bookingServiceURL+f"/bookings",json=body)
    return response

def delete_booking_with_userid(userId):
    response = requests.delete(bookingServiceURL+f"/bookings/users/{userId}")
    return response

def delete_booking_with_userid_showid(userId,showId):
    response = requests.delete(bookingServiceURL+f"/bookings/users/{userId}/shows/{showId}")
    return response

def delete_bookings():
    response = requests.delete(bookingServiceURL+"/bookings")
    return response

def main():
    delete_users()
    delete_bookings()
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

    #get all theatres
    response = get_theatres()
    if(response.status_code!=200):
        print("failed get theatres")
        return
    print("printing theatres")
    print(response.json())

    print("passed test for getting theatres")

    #get shows with theatreid
    response = get_shows_with_theatreid(2)
    if(response.status_code!=200):
        print("failed to get shows with theaterid")
        return
    print("printing shows with theatre id 2")
    print(response.json())

    print("passed test for getting shows with theatre_id")

    #get shows with non-exixting theatreid
    response = get_shows_with_theatreid(70)
    if(response.status_code!=404):
        print("failed to get shows with non-existing theaterid")
        return
    
    print("passed test for getting shows with non-existing theatre_id")

     #get shows with showid
    response = get_shows_with_showid(2)
    if(response.status_code!=200):
        print("failed to get shows with showid")
        return
    print(response.json())

    print("passed test for getting shows with show_id")

    #get shows with non-exixting showid
    response = get_shows_with_showid(70)
    if(response.status_code!=404):
        print("failed to get shows with non-existing showid")
        return

    print("passed test for getting shows with non-existing show_id")

    #get booking for user with no bookings
    response = get_bookings_with_userid(userid1)
    if(response.status_code!=200):
        print("failed to get booking")
        return     
    if(len(response.json())!=0):
        print("failed to get booking for user with no bookings")
        return

    print("passed test for getting booking for user with no bookings")

    #create booking for invalid show
    response = create_booking(userid1,80,1)
    if(response.status_code!=400):
        print("failed invalid show booking")
        return
    
    print("passed test for create booking for invalid show")
    
    #create booking for invalid user
    response = create_booking(1000,2,1)
    if(response.status_code!=400):
        print("failed invalid user booking")
        return
    
    print("passed test for create booking for invalid user")

    #put money in wallet of users
    put_wallet(userid1,"credit",1)
    put_wallet(userid2,"credit",10000)
    put_wallet(userid3,"credit",10000)

    #create booking with less money
    response=create_booking(userid1,2,1)
    if(response.status_code!=400):
        print("failed booking less money")
        return

    print("passed test for create booking with less money")

    #create booking with high no of seats
    response=create_booking(userid2,2,40)
    if(response.status_code!=400):
        print("failed booking high no. of seats")
        return

    print("passed test for create booking with high no of seats")

    #create a booking
    response=create_booking(userid2,2,1)
    if(response.status_code!=200):
        print("failed to create booking")
        return
    
    print("passes test for creating a booking")

    #check balance and seats after booking
    response=get_shows_with_showid(2)
    show=response.json()
    if(show["seats_available"]!=29):
        print("failed seat update")
        return
    response=get_wallet(userid2)
    wallet=response.json()
    if(wallet["balance"]!=(10000-55)):
        print("failed update wallet")
        return

    print("passed test for checking balance and seats after booking")

    #print show 1 and 3
    response=get_shows_with_showid(1)
    show=response.json()
    print("printing shows with show id 1 and 3")
    print(show)
    response=get_shows_with_showid(3)
    show=response.json()
    print(show)

    #create some more bookings
    response=create_booking(userid2,2,1)
    response=create_booking(userid2,3,1)
    response=create_booking(userid3,2,1)
    response=create_booking(userid3,2,1)
    response=create_booking(userid3,3,1)

    #get booking for user2
    response=get_bookings_with_userid(userid2)
    if(response.status_code!=200):
        print("fail to get booking")
    booking=response.json()
    print("printing booking for user2")
    print(booking)

    print("passed test for get booking")

    #delete booking using userid and showid
    response=delete_booking_with_userid_showid(userid2,2)
    if(response.status_code!=200):
        print("fail to delete booking")
        return
    
    print("passed test for delete booking using userid and showid")
    
    #check available seats and wallets balance
    response=get_shows_with_showid(2)
    show=response.json()
    if(show["seats_available"]!=28):
        print("failed seat update after delete")
        return
    response=get_wallet(userid2)
    wallet=response.json()
    if(wallet["balance"]!=(10000-60)):
        print("failed update wallet after delete")
        return
    
    print("passed test to update seats and balance after delete")

    #delete booking using userid
    response=delete_booking_with_userid(userid3)
    print(userid3)
    if(response.status_code!=200):
        print("fail to delete booking")
        return    

    print("passed test for deleting booking with userrid")

    #check available seats and wallets balance
    response=get_shows_with_showid(2)
    show=response.json()
    if(show["seats_available"]!=30):
        print("failed seat update after delete")
        return
    response=get_wallet(userid3)
    wallet=response.json()
    if(wallet["balance"]!=(10000)):
        print("failed update wallet after delete")
        return

    print("passed test to update seats and balance after delete")

    #delete non existing booking
    response=delete_booking_with_userid_showid(userid2,2)
    if(response.status_code!=404):
        print("fail to delete booking")
        return
    response=delete_booking_with_userid(userid3)
    if(response.status_code!=404):
        print("fail to delete booking")
        return

    print("passed test to delete non-existing booking")

    #create bookings for user3
    response=create_booking(userid3,2,1)
    response=create_booking(userid3,2,1)
    response=create_booking(userid3,3,1)
    response=create_booking(userid3,3,1)
    response=create_booking(userid3,5,1)
    response=create_booking(userid3,8,1)
    response=create_booking(userid3,6,1)
    response=create_booking(userid3,4,1)
    response=create_booking(userid3,1,1)
    response=create_booking(userid3,5,1)
    response=create_booking(userid3,9,1)
    response=create_booking(userid3,11,1)
    response=create_booking(userid3,12,1)
    response=create_booking(userid3,13,1)
    response=create_booking(userid3,14,1)
    #delete user and check booking
    response=get_bookings_with_userid(userid3)
    booking=response.json()
    if(len(booking)==0):
        print("failed to get bookings")
        return
    response=delete_user(userid3)
    response=get_bookings_with_userid(userid3)

    booking=response.json()
    if(len(booking)!=0):
        print("failed to remove booking by delete user")
        return

    print("passed test to remove booking after user is deleted")

    #create bookings any try delete all
    put_wallet(userid3,"credit",10000)
    response=create_booking(userid2,2,1)
    response=create_booking(userid2,3,1)
    response=create_booking(userid1,2,1)
    response=create_booking(userid1,2,1)
    response=create_booking(userid1,3,1)
    response=delete_bookings()
    if(response.status_code!=200):
        print("failed to delete bookings")
        return
    response=get_shows_with_showid(2)
    show=response.json()
    if(show["seats_available"]!=30):
        print("failed seat update after delete")
        return
    response=get_wallet(userid3)
    wallet=response.json()
    if(wallet["balance"]!=(10000)):
        print("failed update wallet after delete")
        return
    
    print("passed test for deleting all bookings")

if __name__ == "__main__":
    main()