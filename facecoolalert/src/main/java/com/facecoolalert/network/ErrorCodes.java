package com.facecoolalert.network;

import org.jetbrains.annotations.NotNull;

public final class ErrorCodes {
   @NotNull
   public static final String AUTH_TOO_MAN_REQUESTS = "auth/too-many-requests";
   @NotNull
   public static final String AUTH_USER_NOT_FOUND = "auth/user-not-found";


   @NotNull
   public static final String AUTH_EMAIL_ALREADY_IN_USE = "auth/email-already-in-use";

}