package io.confluent.ad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;

import java.lang.invoke.MethodHandles;

public class Waffle {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // Apparently Waffle only works on non-unix systems :(

    public static void main(String[] args) {
        //waffle.windows.auth.impl.WindowsAuthProviderImpl
        WindowsAuthProviderImpl authenticationProvider = new WindowsAuthProviderImpl();
        IWindowsIdentity loggedOnUser = authenticationProvider.logonUser(Config.USERNAME, Config.PASSWORD);
        LOG.info("Logged in?" +loggedOnUser.isGuest());

    }

}
