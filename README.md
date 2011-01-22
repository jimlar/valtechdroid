# WARNING
This stuff is not at all complete, and may very well delete all the contacts on your phone if installed and executed.

**Use with caution!**

## ValtechDroid
This is an Android SyncAdapter that synchronizes the Valtech Sweden employees into your Android contacts.
The idea is that you will benefit from getting all types of phone numbers (short/mobile/local), images and geo locations from the intranet.

### Android Compatibility
* Requires Android 2.1-update1

### Stuff Done
* Reading the Valtech intranet contacts
* Android sync adapter, to store account info and schedule sync
* Migrate to the Android 2 Contacts APIs
* Batching the ContentProvider operations insert/update operations
* Adds email, mobile phone, local phone and short phone

### Stuff ToDo
* Find a way to do tests in Android that is not pure pain
* Update contact with changed/new info on consecutive executions
* Add the contacts to a group that is visible (instead of the null account type/name)
* Prevent added contacts from being synced to other places
* Add contact image
* Test password in the password dialog
* Add organization to the contact