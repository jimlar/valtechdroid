# WARNING
Use with caution and remember to back up your contacts before you install/enable this app.

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
* Add the contacts to a group that is visible (instead of the null account type/name)
* Add contact image
* Test password in the password dialog
* Update contact with changed/new info on consecutive executions
* Use SyncStats to record the status of the sync execution
* Only update if needed
* Add organization to the contact
* Keep status update history
* Save status message
* Valtech icon
* Remove the delete all option
* Remove the org . role
* Prevent added contacts from being synced to other places

 ### Stuff ToDo
* Handle passwords that start to fail in existing account setup
* Edit password credentials
* Find a way to do tests in Android that is not pure pain
* Save geolocation
* Scale the contact image better
* Only update modified fields
* Better icons
* Market:
  * Better screenshot
  * Better icon
* se.jimlar -> se.valtech
* Default enable sync
