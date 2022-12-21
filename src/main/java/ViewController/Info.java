package ViewController;

import javax.swing.*;

import java.io.IOException;

import static IO.IO.getFileAsIOStream;

public class Info {
    public static char[] blocks = {'0', '1', '2', '3', '4', '5', '6', '+'};


    static ImageIcon playerButtonIcon;

    static {
        try {
            playerButtonIcon = new ImageIcon(getFileAsIOStream("textures/menu/PlayerButtonIcon.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon fourVButtonIcon;

    static {
        try {
            fourVButtonIcon = new ImageIcon(getFileAsIOStream("textures/menu/4VButton.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon fourHButtonIcon;

    static {
        try {
            fourHButtonIcon = new ImageIcon(getFileAsIOStream("textures/menu/4HButton.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon threeVButtonIcon;

    static {
        try {
            threeVButtonIcon = new ImageIcon(getFileAsIOStream("textures/menu/3VButton.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon threeHButtonIcon;

    static {
        try {
            threeHButtonIcon = new ImageIcon(getFileAsIOStream("textures/menu/3HButton.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon twoVButtonIcon;

    static {
        try {
            twoVButtonIcon = new ImageIcon(getFileAsIOStream("textures/menu/2VButton.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon twoHButtonIcon;

    static {
        try {
            twoHButtonIcon = new ImageIcon(getFileAsIOStream("textures/menu/2HButton.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon oneButtonIcon;

    static {
        try {
            oneButtonIcon = new ImageIcon(getFileAsIOStream("textures/menu/1Button.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon eraseButtonIcon;

    static {
        try {
            eraseButtonIcon = new ImageIcon(getFileAsIOStream("textures/menu/eraseButton.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon mainMenuBackground;

    static {
        try {
            mainMenuBackground = new ImageIcon(getFileAsIOStream("textures/menu/mainMenuBackground.gif").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon mainMenuGameTitle;

    static {
        try {
            mainMenuGameTitle = new ImageIcon(getFileAsIOStream("textures/menu/mainMenuGameTitle.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon mainMenuConnectButton;

    static {
        try {
            mainMenuConnectButton = new ImageIcon(getFileAsIOStream("textures/menu/mainMenuConnectToMatchButton.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon mainMenuWaitButton;

    static {
        try {
            mainMenuWaitButton = new ImageIcon(getFileAsIOStream("textures/menu/mainMenuWaitForConnectionButton.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon mainMenuOptionsButton;

    static {
        try {
            mainMenuOptionsButton = new ImageIcon(getFileAsIOStream("textures/menu/mainMenuOptionsButton.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ImageIcon mainMenuExitButton;

    static {
        try {
            mainMenuExitButton = new ImageIcon(getFileAsIOStream("textures/menu/mainMenuExitButton.png").readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
