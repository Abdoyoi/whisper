# Whispr

Whispr is an offline messaging application written in Java with a Swing graphical interface (CS12002 team project).
There is no network: it starts with sample users, contacts and chats, and everything you do can be saved to and loaded from text files.

## Features

- **Landing page:** your name, all chats newest first, each showing the contact name, an unread marker, the last message and its date/time. Menu buttons for Profile, Contacts, Search, New Chat, Save and Load.
- **Chats:** open a chat to see the conversation as message bubbles, reply, delete a single message, or delete the whole chat. Opening a chat marks it as read.
- **New chat:** pick a contact; if you already have a chat with them it is opened instead of making a duplicate.
- **Profile:** view and edit your name and phone number (your user ID is fixed because your messages are linked to it).
- **Contacts:** add, edit, delete, sort A-Z or by most recently added. Phone numbers are validated and duplicates are rejected.
  Double-click (or "View Contact") to see a contact's details and the three most recent messages they sent you; double-click one to open that chat.
  - Deleting a contact asks whether to keep or delete their chats. Kept chats show the phone number instead of the name.
  - Editing a contact's phone number keeps their existing chats linked to them.
- **Search:** enter one or more keywords (a message must contain all of them, ignoring case). Matching **messages** are listed with the keywords in bold.
  Click a result to open its chat scrolled to that message; Back returns to the same results.
- **Save / Load:** writes the user, contacts, chats and messages (including read/unread state) to the `whispr_data` folder. If that folder exists, it is loaded automatically at start-up. On exit you are asked whether to save unsaved changes.

## Architecture

| Package | Contents |
|---|---|
| `model` | `User`, `Contact`, `Message`, `Chat` |
| `datastructures` | `MessageQueue` (singly linked queue), `ChatLinkedList` (doubly linked list), their node classes |
| `managers` | `ContactManager`, `SearchManager`, `FileManager` |
| `gui` | `LandingPanel`, `ChatPanel`, `ContactsPanel`, `ProfilePanel`, `SearchPanel`, plus small helpers |
| `main` | `WhisprApp` (window and navigation), `AppState`, `SampleData`, `ThemeConstants` |

### Data structure choices

- **Messages in a chat: `MessageQueue`**, a linked queue with head and tail pointers, so adding a message is O(1). Deleting or listing messages is O(n).
- **Chats: `ChatLinkedList`**, a doubly linked list, so an unlinked chat does not need other items shifted. Finding a chat by ID is O(n). Sorting by most recent or by name copies to an `ArrayList`, sorts, and rebuilds the list.
- **Contacts: `ArrayList` + `HashSet` of phone numbers.** The `HashSet` makes duplicate detection O(1) instead of scanning every contact. A second list remembers the order contacts were added, so "most recent" still works after sorting A-Z.
- **Links between objects:** chats and messages store phone numbers / user IDs rather than `Contact` objects. A contact can therefore be deleted or edited without breaking the chats.

### File format

Four text files (`user.txt`, `contacts.txt`, `chats.txt`, `messages.txt`) with `|`-separated fields. Any `|`, backslash or line break inside a field is escaped, so messages can contain any text.
Messages are tied to their chat by chat ID, and to their sender by user ID / phone number.

## Setup

Requires Java 11 or later.

```
git clone https://github.com/Abdoyoi/whisper.git
cd whisper
```

Compile and run (macOS / Linux):

```
javac -d out $(find src -name "*.java")
java -cp out main.WhisprApp
```

Compile and run (Windows PowerShell):

```
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out main.WhisprApp
```

To start again with the sample data, delete the `whispr_data` folder.

## Tests

`test/ManagerTests.java` checks the data structures, contacts, search and the save/load round trip (32 checks, including text containing `|`, backslashes and line breaks):

```
javac -cp out -d test-out test/ManagerTests.java
java -cp out:test-out ManagerTests        # use ; instead of : on Windows
```

`test/GuiTest.java` drives the real interface (clicking lists and buttons, saving, loading, deleting) and saves screenshots. It needs a display, or `xvfb-run` on Linux.

## Known limitations

- Single user on one machine; there is no live messaging between users, so incoming messages come only from the sample or saved data.
- Profile pictures are stored as a path but are not displayed.
- Chats are one-to-one in the interface (the data model already allows several participants, so group chats would be a natural extension).
- The sort order of the contacts list is not remembered between runs (contacts are saved in the order they were added).

## AI use acknowledgement

The core application (model classes, custom data structures, managers and first version of the interface) was written by the project author.
Generative AI (Claude, Anthropic) was later used to complete the interface wiring (search, save/load, contacts, chat screen), fix bugs in `FileManager`, `ContactManager` and `Chat`, and write the tests and this README.
The author remains responsible for the content.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
