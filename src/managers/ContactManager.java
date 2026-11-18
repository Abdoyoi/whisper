package managers;

import model.Contact;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Manages the contacts in the application.
 *
 * Two lists are kept: contactList is the order shown on screen (it can be re-sorted),
 * and addedOrder always remembers the order contacts were added, so "most recently added"
 * still works after sorting A-Z and the contacts are saved in a stable order.
 * A HashSet of phone numbers gives constant-time duplicate detection.
 */
public class ContactManager {
    private ArrayList<Contact> contactList;
    private ArrayList<Contact> addedOrder;
    private HashSet<String> phoneNumbers;

    public ContactManager() {
        this.contactList = new ArrayList<>();
        this.addedOrder = new ArrayList<>();
        this.phoneNumbers = new HashSet<>();
    }

    /** Adds a new contact. Throws IllegalArgumentException if the phone number is already used. */
    public void addContact(Contact contact) {
        if (phoneNumbers.add(contact.getPhoneNumber())) {
            contactList.add(contact);
            addedOrder.add(contact);
        } else {
            throw new IllegalArgumentException("Contact with this phone number already exists.");
        }
    }

    /** Replaces the contact at the given (displayed) index. */
    public void editContact(int index, Contact updated) {
        if (index < 0 || index >= contactList.size()) {
            throw new IndexOutOfBoundsException("Invalid contact index.");
        }
        Contact existing = contactList.get(index);
        phoneNumbers.remove(existing.getPhoneNumber());
        if (phoneNumbers.add(updated.getPhoneNumber())) {
            contactList.set(index, updated);
            addedOrder.set(indexOfIdentity(addedOrder, existing), updated);
        } else {
            phoneNumbers.add(existing.getPhoneNumber()); // restore: the edit was rejected
            throw new IllegalArgumentException("Contact with this phone number already exists.");
        }
    }

    /** Deletes the contact at the given (displayed) index. */
    public void deleteContact(int index) {
        if (index < 0 || index >= contactList.size()) {
            throw new IndexOutOfBoundsException("Invalid contact index.");
        }
        Contact contact = contactList.remove(index);
        addedOrder.remove(indexOfIdentity(addedOrder, contact));
        phoneNumbers.remove(contact.getPhoneNumber());
    }

    /** Sorts the displayed list alphabetically by name. */
    public void sortAlphabetically() {
        contactList.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
    }

    /** Sorts the displayed list with the most recently added contact first. */
    public void sortByRecent() {
        ArrayList<Contact> recent = new ArrayList<>();
        for (int i = addedOrder.size() - 1; i >= 0; i--) recent.add(addedOrder.get(i));
        contactList = recent;
    }

    public Contact getContactByPhone(String phone) {
        for (Contact contact : contactList) {
            if (contact.getPhoneNumber().equals(phone)) {
                return contact;
            }
        }
        return null;
    }

    /** Contacts in the order they are currently displayed. */
    public ArrayList<Contact> getAllContacts() {
        return new ArrayList<>(contactList);
    }

    /** Contacts in the order they were added (oldest first). Used when saving. */
    public ArrayList<Contact> getContactsInAddedOrder() {
        return new ArrayList<>(addedOrder);
    }

    public int size() {
        return contactList.size();
    }

    private static int indexOfIdentity(ArrayList<Contact> list, Contact target) {
        for (int i = 0; i < list.size(); i++) if (list.get(i) == target) return i;
        throw new IllegalStateException("Contact lists are out of sync.");
    }
}
