Reflection

GitHub Link:
https://github.com/COSC1295-advanced-programming-2025-s2/cosc1295-assignment-2-semester-2-2025-purnaperamune

The RMIT CareHome system was designed as a modular, object oriented console
level application to manage daily operations in a care home such as adding new residents,
moving residents from one ward to another, writing prescription by doctors, administration of medicine by
nurse and many more.

This system is consisting with text based menu as the interface to interact with
the system.

Use of Data Structures
----------------------

To implement wards, rooms, and beds, I have used list (ArrayList) to maintain insertion orders and
it enables 1-based access easily. Simplicity and maintainability is high with
using functions such as add(), remove(), when using arraylists. Also,
to display data about occupancy in these wards,rooms and beds, we have to traverse hierarchical
data such as Ward -> Room -> Bed. Use of arraylist makes a better option for such activities due to
its efficiently for reading heavy loads, and fast access. Additionally, arraylists can grow and shrink
dynamically, unlike arrays which are fixed in size. I could have use Map data structure as well
for this, but it would add unwanted complexity.

To implement Staff details such as Doctors, Nurses and Managers as well, I have used list (ArrayList),
because of its ability to grow and shrink dynamically. We do not need to provide
a length/size during the initialization of this variables. As we do not know, how many
staff is needed to add in the future, arraylists was selected as the suitable data structure.

The program heavily depends on OOP concepts which are Abstraction, Polymorphism, Inheritance and Encapsulation.
Each entity used in this program such as Resident, Nurse, Room, Ward's
are encapsulated. Which means member variables declared inside these class are
private and cannot modify directly, enabling to access them through public methods.

Inheritance can be seen among classes such as Doctor, Nurse, and Manager where I
implemented a super class named Staff to include common characteristics of these classes.
This helps to avoid code duplications and enables polymorphic handling of all staff through a single reference type.

Representation of all the classes with its member variables and methods together are
examples of use of abstraction.

The authenticate() method in CareHome returns a general Staff type,
but the caller can operate on Doctor or Nurse depending on the actual subclass
is an example for the use of Polymorphism.
