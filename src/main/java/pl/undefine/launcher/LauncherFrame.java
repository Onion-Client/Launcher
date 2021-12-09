/*
 * Copyright (c) 2021, Undefine <cqundefine@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package pl.undefine.launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LauncherFrame extends JFrame implements ActionListener
{
    JButton playButton;
    JLabel titleLabel;

    int width;
    int height;

    public LauncherFrame(int width, int height)
    {
        System.out.println("Creating launcher frame...");

        this.width = width;
        this.height = height;

        setTitle("Onion Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(width, height);
        setVisible(true);
        setLayout(null);
        getContentPane().setBackground(Color.DARK_GRAY);

        titleLabel = new JLabel("Onion Launcher");
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Calibri", Font.PLAIN, 75));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 0, width, 200);
        add(titleLabel);

        playButton = new JButton("Play");
        playButton.setBounds(width / 4, height / 4 * 3 - 100, width / 2, 100);
        playButton.addActionListener(this);
        add(playButton);
    }

    @Override
    public void actionPerformed(ActionEvent event)
    {
        if (event.getSource() == playButton)
        {
            System.out.println("Launching minecraft...");
            MinecraftUtils.EnsureRootIntegrity();
        }
    }
}
