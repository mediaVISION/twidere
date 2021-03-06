/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.model;

import static org.mariotaku.twidere.util.Utils.formatDirectMessageText;
import static org.mariotaku.twidere.util.Utils.getAsBoolean;
import static org.mariotaku.twidere.util.Utils.getAsLong;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.parseString;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import org.mariotaku.twidere.provider.TweetStore.DirectMessages;

import twitter4j.DirectMessage;
import twitter4j.User;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableDirectMessage implements Parcelable, Serializable, Comparable<ParcelableDirectMessage> {

	private static final long serialVersionUID = -3721836808981416526L;

	public static final Parcelable.Creator<ParcelableDirectMessage> CREATOR = new Parcelable.Creator<ParcelableDirectMessage>() {
		@Override
		public ParcelableDirectMessage createFromParcel(final Parcel in) {
			return new ParcelableDirectMessage(in);
		}

		@Override
		public ParcelableDirectMessage[] newArray(final int size) {
			return new ParcelableDirectMessage[size];
		}
	};

	public static final Comparator<ParcelableDirectMessage> MESSAGE_ID_COMPARATOR = new Comparator<ParcelableDirectMessage>() {

		@Override
		public int compare(final ParcelableDirectMessage object1, final ParcelableDirectMessage object2) {
			final long diff = object2.message_id - object1.message_id;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public final long account_id, message_id, message_timestamp;

	public final long sender_id, recipient_id;
	public final boolean is_out_going;

	public final String text_html, text_plain;

	public final String sender_name, recipient_name, sender_screen_name, recipient_screen_name;
	public final String sender_profile_image_url, recipient_profile_image_url;

	public ParcelableDirectMessage(final ContentValues values) {
		text_plain = values.getAsString(DirectMessages.TEXT_PLAIN);
		text_html = values.getAsString(DirectMessages.TEXT_HTML);
		sender_screen_name = values.getAsString(DirectMessages.SENDER_SCREEN_NAME);
		sender_profile_image_url = values.getAsString(DirectMessages.SENDER_PROFILE_IMAGE_URL);
		sender_name = values.getAsString(DirectMessages.SENDER_NAME);
		sender_id = getAsLong(values, DirectMessages.SENDER_ID, -1);
		recipient_screen_name = values.getAsString(DirectMessages.RECIPIENT_SCREEN_NAME);
		recipient_profile_image_url = values.getAsString(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL);
		recipient_name = values.getAsString(DirectMessages.RECIPIENT_NAME);
		recipient_id = getAsLong(values, DirectMessages.RECIPIENT_ID, -1);
		message_timestamp = getAsLong(values, DirectMessages.MESSAGE_TIMESTAMP, -1);
		message_id = getAsLong(values, DirectMessages.MESSAGE_ID, -1);
		is_out_going = getAsBoolean(values, DirectMessages.IS_OUTGOING, false);
		account_id = getAsLong(values, DirectMessages.ACCOUNT_ID, -1);
	}

	public ParcelableDirectMessage(final Cursor cursor, final DirectMessageCursorIndices indices) {
		account_id = indices.account_id != -1 ? cursor.getLong(indices.account_id) : -1;
		is_out_going = indices.is_outgoing != -1 ? cursor.getShort(indices.is_outgoing) == 1 : null;
		message_id = indices.message_id != -1 ? cursor.getLong(indices.message_id) : -1;
		message_timestamp = indices.message_timestamp != -1 ? cursor.getLong(indices.message_timestamp) : -1;
		sender_id = indices.sender_id != -1 ? cursor.getLong(indices.sender_id) : -1;
		recipient_id = indices.recipient_id != -1 ? cursor.getLong(indices.recipient_id) : -1;
		text_html = indices.text != -1 ? cursor.getString(indices.text) : null;
		text_plain = indices.text_plain != -1 ? cursor.getString(indices.text_plain) : null;
		sender_name = indices.sender_name != -1 ? cursor.getString(indices.sender_name) : null;
		recipient_name = indices.recipient_name != -1 ? cursor.getString(indices.recipient_name) : null;
		sender_screen_name = indices.sender_screen_name != -1 ? cursor.getString(indices.sender_screen_name) : null;
		recipient_screen_name = indices.recipient_screen_name != -1 ? cursor.getString(indices.recipient_screen_name)
				: null;
		sender_profile_image_url = indices.sender_profile_image_url != -1 ? cursor
				.getString(indices.sender_profile_image_url) : null;
		recipient_profile_image_url = indices.recipient_profile_image_url != -1 ? cursor
				.getString(indices.recipient_profile_image_url) : null;
	}

	public ParcelableDirectMessage(final DirectMessage message, final long account_id, final boolean is_outgoing,
			final boolean large_profile_image) {
		this.account_id = account_id;
		is_out_going = is_outgoing;
		final User sender = message.getSender(), recipient = message.getRecipient();
		final String sender_profile_image_url_string = sender != null ? parseString(sender.getProfileImageUrlHttps())
				: null;
		final String recipient_profile_image_url_string = recipient != null ? parseString(recipient
				.getProfileImageUrlHttps()) : null;
		message_id = message.getId();
		message_timestamp = getTime(message.getCreatedAt());
		sender_id = sender != null ? sender.getId() : -1;
		recipient_id = recipient != null ? recipient.getId() : -1;
		text_html = formatDirectMessageText(message);
		text_plain = message.getText();
		sender_name = sender != null ? sender.getName() : null;
		recipient_name = recipient != null ? recipient.getName() : null;
		sender_screen_name = sender != null ? sender.getScreenName() : null;
		recipient_screen_name = recipient != null ? recipient.getScreenName() : null;
		sender_profile_image_url = large_profile_image ? getBiggerTwitterProfileImage(sender_profile_image_url_string)
				: sender_profile_image_url_string;
		recipient_profile_image_url = large_profile_image ? getBiggerTwitterProfileImage(recipient_profile_image_url_string)
				: recipient_profile_image_url_string;
	}

	public ParcelableDirectMessage(final Parcel in) {
		account_id = in.readLong();
		message_id = in.readLong();
		message_timestamp = in.readLong();
		sender_id = in.readLong();
		recipient_id = in.readLong();
		is_out_going = in.readInt() == 1;
		text_html = in.readString();
		text_plain = in.readString();
		sender_name = in.readString();
		recipient_name = in.readString();
		sender_screen_name = in.readString();
		recipient_screen_name = in.readString();
		sender_profile_image_url = in.readString();
		recipient_profile_image_url = in.readString();
	}

	@Override
	public int compareTo(final ParcelableDirectMessage another) {
		if (another == null) return 0;
		final long diff = another.message_id - message_id;
		if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
		if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
		return (int) diff;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ParcelableDirectMessage)) return false;
		final ParcelableDirectMessage other = (ParcelableDirectMessage) obj;
		if (account_id != other.account_id) return false;
		if (message_id != other.message_id) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (account_id ^ account_id >>> 32);
		result = prime * result + (int) (message_id ^ message_id >>> 32);
		return result;
	}

	@Override
	public String toString() {
		return "ParcelableDirectMessage{account_id=" + account_id + ", message_id=" + message_id
				+ ", message_timestamp=" + message_timestamp + ", sender_id=" + sender_id + ", recipient_id="
				+ recipient_id + ", is_out_going=" + is_out_going + ", text=" + text_html + ", sender_name="
				+ sender_name + ", recipient_name=" + recipient_name + ", sender_screen_name=" + sender_screen_name
				+ ", recipient_screen_name=" + recipient_screen_name + ", sender_profile_image_url="
				+ sender_profile_image_url + ", recipient_profile_image_url=" + recipient_profile_image_url + "}";
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeLong(account_id);
		out.writeLong(message_id);
		out.writeLong(message_timestamp);
		out.writeLong(sender_id);
		out.writeLong(recipient_id);
		out.writeInt(is_out_going ? 1 : 0);
		out.writeString(text_html);
		out.writeString(text_plain);
		out.writeString(sender_name);
		out.writeString(recipient_name);
		out.writeString(sender_screen_name);
		out.writeString(recipient_screen_name);
		out.writeString(sender_profile_image_url);
		out.writeString(recipient_profile_image_url);
	}

	private long getTime(final Date date) {
		return date != null ? date.getTime() : 0;
	}
}
