/*
package candor.flare;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

public class Hudai {


    private static final String TAG ="hudai" ;




    -------------------GET A DOCUMENT -------------
    DocumentReference docRef = db.collection("cities").document("SF");
docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        }
    });


// -----------------GET CUSTOM OBJECTS ---------------


DocumentReference docRef = db.collection("cities").document("BJ");
docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
            City city = documentSnapshot.toObject(City.class);
        }
    });





// ---------------------GET MULTIPLE DOCUMENTS ---------------

    db.collection("cities")
            .whereEqualTo("capital", true)
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d(TAG, document.getId() + " => " + document.getData());
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        }
    });





    // -----------------GET ALL DOCUMENTS ---------------------


    db.collection("cities")
            .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d(TAG, document.getId() + " => " + document.getData());
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        }
    });



    // ----------------- GET REALTIME UPDATES -----------------
    final DocumentReference docRef = db.collection("cities").document("SF");
docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(@Nullable DocumentSnapshot snapshot,
                @Nullable FirebaseFirestoreException e) {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: " + snapshot.getData());
            } else {
                Log.d(TAG, "Current data: null");
            }
        }
    });


//      -----------------LISTEN TO MULTIPLE DOCUMENTS -----------------

    db.collection("cities")
            .whereEqualTo("state", "CA")
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot value,
                @Nullable FirebaseFirestoreException e) {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            List<String> cities = new ArrayList<>();
            for (QueryDocumentSnapshot doc : value) {
                if (doc.get("name") != null) {
                    cities.add(doc.getString("name"));
                }
            }
            Log.d(TAG, "Current cites in CA: " + cities);
        }
    });


    // --------------------DETECT TYPES OF CHANGES ----------------------

    db.collection("cities")
            .whereEqualTo("state", "CA")
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot snapshots,
                @Nullable FirebaseFirestoreException e) {
            if (e != null) {
                Log.w(TAG, "listen:error", e);
                return;
            }

            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                switch (dc.getType()) {
                    case ADDED:
                        Log.d(TAG, "New city: " + dc.getDocument().getData());
                        break;
                    case MODIFIED:
                        Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                        break;
                    case REMOVED:
                        Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                        break;
                }
            }

        }
    });



    -------------DETATCH A LISTENER --------------------

    Query query = db.collection("cities");
    ListenerRegistration registration = query.addSnapshotListener(
            new EventListener<QuerySnapshot>() {
                // ...
            });

// ...

// Stop listening to changes
registration.remove();


        -----------------DELETE A DOCUMENT --------------

    db.collection("cities").document("DC")
        .delete()
        .addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Log.d(TAG, "DocumentSnapshot successfully deleted!");
        }
    })
            .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Log.w(TAG, "Error deleting document", e);
        }
    });


    // --------------------  DELETE A FIED --------------

    DocumentReference docRef = db.collection("cities").document("BJ");

    // Remove the 'capital' field from the document
    Map<String,Object> updates = new HashMap<>();
updates.put("capital", FieldValue.delete());

docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {



        // ------------------- BATCH WRITE --------------

        // Get a new write batch
        WriteBatch batch = db.batch();

// Set the value of 'NYC'
        DocumentReference nycRef = db.collection("cities").document("NYC");
        batch.set(nycRef, new City());

// Update the population of 'SF'
        DocumentReference sfRef = db.collection("cities").document("SF");
        batch.update(sfRef, "population", 1000000L);

// Delete the city 'LA'
        DocumentReference laRef = db.collection("cities").document("LA");
        batch.delete(laRef);

// Commit the batch
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // ...
            }
        });




    }
*/
