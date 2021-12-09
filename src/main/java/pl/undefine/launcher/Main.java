/*
 * Copyright (c) 2021, Undefine <cqundefine@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package pl.undefine.launcher;

import javax.swing.*;
import java.awt.*;

public class Main
{
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Onion Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(720,640);
        frame.setVisible(true);
        frame.getContentPane().setBackground(Color.DARK_GRAY);
    }
}
