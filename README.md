# ImageMyContacts

This Android application helps fill contact photos by searching images online.

## Features

* Requests permission to read and modify contacts on first launch.
* Lists contacts without photos and shows an image suggestion for each.
* Swipe right to accept the image and assign it to the contact.
* Swipe left to reject and load another image.
* Delete an invalid contact using the cross button with confirmation.
* Progress bar shows how many contacts have a photo.

This project is provided as a minimal example and uses Glide for image
loading. The image search uses Google Images via a simple HTTP query.
