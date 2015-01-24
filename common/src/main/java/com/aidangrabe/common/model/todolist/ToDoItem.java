package com.aidangrabe.common.model.todolist;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by aidan on 09/01/15.
 * A class representing a single item of a ToDoList
 * A ToDoItem is considered complete when it's completionDate is not 'null'
 */
public class ToDoItem implements Parcelable {

    private int id;
    private String title;
    private Date creationDate, completionDate;


    public ToDoItem(String title) {
        this(title, new Date());
    }

    public ToDoItem(String title, Date creationDate) {
        this(title, creationDate, null);
    }

    public ToDoItem(String title, Date creationDate, Date completionDate) {
        this(-1, title, creationDate, completionDate);
    }

    public ToDoItem(int id, String title, Date creationDate, Date completionDate) {
        this.id = id;
        this.title = title;
        this.creationDate = creationDate;
        this.completionDate = completionDate;
    }

    public ToDoItem(Parcel parcel) {
        this(parcel.readInt(), parcel.readString(),
                new Date(parcel.readLong()),
                new Date(parcel.readLong()));
    }

    /**
     * Complete the ToDoItem and set it's completion date to the current time
     */
    public void complete() {
        setCompletionDate(new Date());
    }

    public boolean isCompleted() {
        return completionDate != null;
    }

    // getters + setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeLong(creationDate.getTime());
        dest.writeLong(completionDate.getTime());
    }

}
