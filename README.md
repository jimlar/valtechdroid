## WARNING
This stuff is not at all complete, and may very well delete all the contacts on your phone if installed and executed.
# Use with caution!

## Valtechdroid
This is an Android application that synchronizes the Valtech Sweden employees into your Android contacts.
The idea is that you will benefit from getting all types phone numbers in (short/long mobile area code and local area code) and also get images and location info from the intranet.

## Compatibility
* This app will require Android 2.0

## Done
* Reading the Valtech intranet contacts
* Android sync adapter, to store account info and schedule sync
* Migrate to the Android 2 Contacts APIs
  -> http://developer.android.com/resources/samples/ContactManager/src/com/example/android/contactmanager/ContactAdder.html
* Batching the ContentProvider operations insert/update operations
* Adds email, mobile phone, local phone and short phone

## TODO
* Find a way to do tests in Android that is not pure pain
* Update contact with changed/new info on consecutive executions
* Add the contacts to a group that is visible
* Prevent added contacts from beeing synced to other places
* Add contact image

