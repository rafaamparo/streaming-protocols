/* ------------------
   Client
   usage: java Client [Server hostname] [Server RTSP listening port] [Video file requested]
   ---------------------- */

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

public class Client { // GUI
  // ----
  JFrame f = new JFrame("RTSP Streaming Client");
  JButton setupButton = new JButton("Setup");
  JButton playButton = new JButton("Play");
  JButton pauseButton = new JButton("Pause");
  JButton advanceButton = new JButton("Advance");
  JButton backButton = new JButton("Go Back");
  JButton tearButton = new JButton("Teardown");
  JPanel mainPanel = new JPanel();
  JPanel buttonPanel = new JPanel();
  JLabel iconLabel = new JLabel();
  ImageIcon icon;

  // RTP variables:
  // ----------------
  DatagramPacket rcvdp; // UDP packet received from the server
  DatagramSocket RTPsocket; // socket to be used to send and receive UDP packets
  static int RTP_RCV_PORT = 25000; // port where the client will receive the RTP packets

  Timer timer; // timer used to receive data from the UDP socket
  byte[] buf; // buffer used to store data received from the server

  // RTSP variables
  // ----------------
  // rtsp states
  final static int INIT = 0;
  final static int READY = 1;
  final static int PLAYING = 2;
  static int state; // RTSP state == INIT or READY or PLAYING
  Socket RTSPsocket; // socket used to send/receive RTSP messages
  // input and output stream filters
  static BufferedReader RTSPBufferedReader;
  static BufferedWriter RTSPBufferedWriter;
  static String VideoFileName; // video file to request to the server
  int RTSPSeqNb = 0; // Sequence number of RTSP messages within the session
  int RTSPid = 0; // ID of the RTSP session (given by the RTSP Server)

  final static String CRLF = "\r\n";

  // Video constants:
  // ------------------
  static int MJPEG_TYPE = 26; // RTP payload type for MJPEG video

  // --------------------------
  // Constructor
  // --------------------------
  public Client() {

    // build GUI
    // --------------------------

    // Frame
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    }); // Buttons buttonPanel.setLayout(new GridLayout(1, 0, 5, 0)); // Reduce spacing
        // between buttons
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Reduce padding

    // Style the buttons
    setupButton.setBackground(new Color(66, 134, 244));
    setupButton.setForeground(Color.WHITE);
    setupButton.setFont(new Font("Arial", Font.BOLD, 14));
    setupButton.setFocusPainted(false);
    setupButton.setBorder(BorderFactory.createRaisedBevelBorder());

    playButton.setBackground(new Color(76, 175, 80));
    playButton.setForeground(Color.WHITE);
    playButton.setFont(new Font("Arial", Font.BOLD, 14));
    playButton.setFocusPainted(false);
    playButton.setBorder(BorderFactory.createRaisedBevelBorder());

    pauseButton.setBackground(new Color(255, 193, 7));
    pauseButton.setForeground(Color.WHITE);
    pauseButton.setFont(new Font("Arial", Font.BOLD, 14));
    pauseButton.setFocusPainted(false);
    pauseButton.setBorder(BorderFactory.createRaisedBevelBorder());

    advanceButton.setBackground(new Color(156, 39, 176)); // Purple
    advanceButton.setForeground(Color.WHITE);
    advanceButton.setFont(new Font("Arial", Font.BOLD, 14));
    advanceButton.setFocusPainted(false);
    advanceButton.setBorder(BorderFactory.createRaisedBevelBorder());

    backButton.setBackground(new Color(0, 188, 212)); // Cyan
    backButton.setForeground(Color.WHITE);
    backButton.setFont(new Font("Arial", Font.BOLD, 14));
    backButton.setFocusPainted(false);
    backButton.setBorder(BorderFactory.createRaisedBevelBorder());

    tearButton.setBackground(new Color(244, 67, 54));
    tearButton.setForeground(Color.WHITE);
    tearButton.setFont(new Font("Arial", Font.BOLD, 14));
    tearButton.setFocusPainted(false);
    tearButton.setBorder(BorderFactory.createRaisedBevelBorder());
    buttonPanel.add(setupButton);
    buttonPanel.add(playButton);
    buttonPanel.add(pauseButton);
    buttonPanel.add(backButton);
    buttonPanel.add(advanceButton);
    buttonPanel.add(tearButton);

    setupButton.addActionListener(new setupButtonListener());
    playButton.addActionListener(new playButtonListener());
    pauseButton.addActionListener(new pauseButtonListener());
    advanceButton.addActionListener(new advanceButtonListener());
    backButton.addActionListener(new backButtonListener());
    tearButton.addActionListener(new tearButtonListener());

    // Image display label
    iconLabel.setIcon(null); // frame layout
    mainPanel.setLayout(null);
    mainPanel.setBackground(new Color(240, 240, 240));
    mainPanel.add(iconLabel);
    mainPanel.add(buttonPanel);

    // Increase video display area
    iconLabel.setBounds(0, 0, 640, 480);
    buttonPanel.setBounds(0, 480, 640, 60);

    f.getContentPane().add(mainPanel, BorderLayout.CENTER);
    f.setSize(new Dimension(650, 580));
    f.setLocationRelativeTo(null); // Center the window
    f.setVisible(true);

    // init timer
    // --------------------------
    timer = new Timer(20, new timerListener());
    timer.setInitialDelay(0);
    timer.setCoalesce(true); // allocate enough memory for the buffer used to receive data from the server
    buf = new byte[65536]; // Increased buffer size for larger frames
  }

  // ------------------------------------
  // main
  // ------------------------------------
  public static void main(String argv[]) throws Exception {
    // Create a Client object
    Client theClient = new Client();

    // get server RTSP port and IP address from the command line
    // ------------------
    int RTSP_server_port = Integer.parseInt(argv[1]);
    String ServerHost = argv[0];
    InetAddress ServerIPAddr = InetAddress.getByName(ServerHost);

    // get video filename to request:
    VideoFileName = argv[2];

    // Establish a TCP connection with the server to exchange RTSP messages
    // ------------------
    theClient.RTSPsocket = new Socket(ServerIPAddr, RTSP_server_port);

    // Set input and output stream filters:
    RTSPBufferedReader = new BufferedReader(new InputStreamReader(theClient.RTSPsocket.getInputStream()));
    RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(theClient.RTSPsocket.getOutputStream()));

    // init RTSP state:
    state = INIT;
  }

  // ------------------------------------
  // Handler for buttons
  // ------------------------------------

  // .............
  // TO COMPLETE
  // .............

  // Handler for Setup button
  // -----------------------
  class setupButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {

      // System.out.println("Setup Button pressed !");

      if (state == INIT) {
        // Init non-blocking RTPsocket that will be used to receive data
        try {
          RTPsocket = new DatagramSocket(RTP_RCV_PORT);
          RTPsocket.setSoTimeout(100); // Increased timeout for better frame reception
        } catch (SocketException se) {
          System.out.println("Socket exception: " + se);
          System.exit(0);
        }

        // init RTSP sequence number
        RTSPSeqNb = 1;

        // Send SETUP message to the server
        send_RTSP_request("SETUP");

        // Wait for the response
        if (parse_server_response() != 200)
          System.out.println("Invalid Server Response");
        else {
          state = READY; // change RTSP state and print out new state
          System.out.println("New RTSP state: READY");
        }
      }else if(state!=INIT)

    {
      System.out.println("do nothing! Invalid state");
    }
  }
}

// Handler for Play button
// -----------------------
class playButtonListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {

    // System.out.println("Play Button pressed !");

    if (state == READY) {
      // increase RTSP sequence number
      // .....
      RTSPSeqNb++;

      // Send PLAY message to the server
      send_RTSP_request("PLAY");

      // Wait for the response
      if (parse_server_response() != 200)
        System.out.println("Invalid Server Response");
      else {
        state = PLAYING; // change RTSP state and print out new state
        System.out.println("New RTSP state: PLAYING");
        timer.start();
      }
    } else if (state != READY) {
      System.out.println("do nothing! Invalid state");
    }
  }
}

// Handler for Pause button
// -----------------------
class pauseButtonListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {

    // System.out.println("Pause Button pressed !");

    if (state == PLAYING) {
      // increase RTSP sequence number
      // ........
      RTSPSeqNb++;
      // Send PAUSE message to the server
      send_RTSP_request("PAUSE");

      // Wait for the response
      if (parse_server_response() != 200)
        System.out.println("Invalid Server Response");
      else {
        // change RTSP state and print out new state
        // ........
        // System.out.println("New RTSP state: ...");
        state = READY;
        System.out.println("New RTSP state: READY");
        // stop the timer
        timer.stop();
      }
    } else if (state != PLAYING) {
      System.out.println("do nothing! Invalid state");
    }
  }
}

// Handler for Teardown button
// -----------------------
class tearButtonListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {

    // System.out.println("Teardown Button pressed !");

    // increase RTSP sequence number
    // ..........
    RTSPSeqNb++;

    // Send TEARDOWN message to the server
    send_RTSP_request("TEARDOWN");

    // Wait for the response
    if (parse_server_response() != 200)
      System.out.println("Invalid Server Response");
    else {
      state = INIT;
      System.out.println("New RTSP state: INIT");
      timer.stop();
      System.exit(0);
    }
  }
} // Handler for Advance button
// -----------------------

class advanceButtonListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {
    System.out.println("Advance Button pressed!");

    if (state == READY || state == PLAYING) {
      boolean wasPlaying = (state == PLAYING);

      // If playing, pause the timer temporarily to avoid conflicts
      if (wasPlaying) {
        timer.stop();
      }

      // increase RTSP sequence number
      RTSPSeqNb++;
      System.out.println("Sending ADVANCE request, sequence number: " + RTSPSeqNb);

      // Send ADVANCE message to the server (custom RTSP command)
      send_RTSP_request("ADVANCE");

      // Wait for the response
      int response = parse_server_response();
      System.out.println("Received response code: " + response);

      if (response != 200) {
        System.out.println("Invalid Server Response");
      } else {
        System.out.println("Advanced to next frame");

        // Force frame update by explicitly receiving a new frame
        try {
          // We need to receive the RTP packet that the server sent after ADVANCE
          DatagramPacket framePacket = new DatagramPacket(buf, buf.length);
          RTPsocket.setSoTimeout(1000); // Set a longer timeout to wait for frame
          RTPsocket.receive(framePacket);
          RTPsocket.setSoTimeout(5); // Reset timeout to original value

          // Create an RTP packet from the received data
          RTPpacket rtp_packet = new RTPpacket(framePacket.getData(), framePacket.getLength());

          System.out.println("Received frame after ADVANCE: SeqNum # " +
              rtp_packet.getsequencenumber() + " TimeStamp " + rtp_packet.gettimestamp());

          // Get the payload and update the display
          int payload_length = rtp_packet.getpayload_length();
          byte[] payload = new byte[payload_length];
          rtp_packet.getpayload(payload);

          // Update the image display
          Toolkit toolkit = Toolkit.getDefaultToolkit();
          Image image = toolkit.createImage(payload, 0, payload_length);
          icon = new ImageIcon(image);
          iconLabel.setIcon(icon);
        } catch (Exception ex) {
          System.out.println("Error receiving frame after ADVANCE: " + ex);
        }
      }

      // If was playing, restart the timer
      if (wasPlaying) {
        timer.start();
      }
    } else {
      System.out.println("Cannot advance: Invalid state");
    }
  }
} // Handler for Go Back button
// -----------------------

class backButtonListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {
    System.out.println("Go Back Button pressed!");

    if (state == READY || state == PLAYING) {
      boolean wasPlaying = (state == PLAYING);

      // If playing, pause the timer temporarily to avoid conflicts
      if (wasPlaying) {
        timer.stop();
      }

      // increase RTSP sequence number
      RTSPSeqNb++;
      System.out.println("Sending BACK request, sequence number: " + RTSPSeqNb);

      // Send BACK message to the server (custom RTSP command)
      send_RTSP_request("BACK");

      // Wait for the response
      int response = parse_server_response();
      System.out.println("Received response code: " + response);

      if (response != 200) {
        System.out.println("Invalid Server Response");
      } else {
        System.out.println("Went back to previous frame");

        // Force frame update by explicitly receiving a new frame
        try {
          // We need to receive the RTP packet that the server sent after BACK
          DatagramPacket framePacket = new DatagramPacket(buf, buf.length);
          RTPsocket.setSoTimeout(1000); // Set a longer timeout to wait for frame
          RTPsocket.receive(framePacket);
          RTPsocket.setSoTimeout(5); // Reset timeout to original value

          // Create an RTP packet from the received data
          RTPpacket rtp_packet = new RTPpacket(framePacket.getData(), framePacket.getLength());

          System.out.println("Received frame after BACK: SeqNum # " +
              rtp_packet.getsequencenumber() + " TimeStamp " + rtp_packet.gettimestamp());

          // Get the payload and update the display
          int payload_length = rtp_packet.getpayload_length();
          byte[] payload = new byte[payload_length];
          rtp_packet.getpayload(payload);

          // Update the image display
          Toolkit toolkit = Toolkit.getDefaultToolkit();
          Image image = toolkit.createImage(payload, 0, payload_length);
          icon = new ImageIcon(image);
          iconLabel.setIcon(icon);
        } catch (Exception ex) {
          System.out.println("Error receiving frame after BACK: " + ex);
        }
      }

      // If was playing, restart the timer
      if (wasPlaying) {
        timer.start();
      }
    } else {
      System.out.println("Cannot go back: Invalid state");
    }
  }
}

// ------------------------------------
// Handler for timer
// ------------------------------------

class timerListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {

    // Construct a DatagramPacket to receive data from the UDP socket
    rcvdp = new DatagramPacket(buf, buf.length);

    try {
      // receive the DP from the socket:
      RTPsocket.receive(rcvdp);

      // create an RTPpacket object from the DP
      RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());

      // print important header fields of the RTP packet received:
      System.out.println("Got RTP packet with SeqNum # " + rtp_packet.getsequencenumber() + " TimeStamp "
          + rtp_packet.gettimestamp() + " ms, of type " + rtp_packet.getpayloadtype());

      // print header bitstream:
      rtp_packet.printheader();

      // get the payload bitstream from the RTPpacket object
      int payload_length = rtp_packet.getpayload_length();
      byte[] payload = new byte[payload_length];
      rtp_packet.getpayload(payload);

      // get an Image object from the payload bitstream
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Image image = toolkit.createImage(payload, 0, payload_length);

      // display the image as an ImageIcon object
      icon = new ImageIcon(image);
      iconLabel.setIcon(icon);
    } catch (InterruptedIOException iioe) {
      // System.out.println("Nothing to read");
    } catch (IOException ioe) {
      System.out.println("Exception caught: " + ioe);
    }
  }

  }

  // ------------------------------------
  // Parse Server Response
  // ------------------------------------
  private int parse_server_response() {
    int reply_code = 0;

    try {
      // parse status line and extract the reply_code:
      String StatusLine = RTSPBufferedReader.readLine();
      // System.out.println("RTSP Client - Received from Server:");
      System.out.println(StatusLine);

      StringTokenizer tokens = new StringTokenizer(StatusLine);
      tokens.nextToken(); // skip over the RTSP version
      reply_code = Integer.parseInt(tokens.nextToken());

      // if reply code is OK get and print the 2 other lines
      if (reply_code == 200) {
        String SeqNumLine = RTSPBufferedReader.readLine();
        System.out.println(SeqNumLine);

        String SessionLine = RTSPBufferedReader.readLine();
        System.out.println(SessionLine);

        // if state == INIT gets the Session Id from the SessionLine
        tokens = new StringTokenizer(SessionLine);
        tokens.nextToken(); // skip over the Session:
        RTSPid = Integer.parseInt(tokens.nextToken());
      }
    } catch (Exception ex) {
      System.out.println("Exception caught: " + ex);
      System.exit(0);
    }

    return (reply_code);
  }

  // ------------------------------------
  // Send RTSP Request
  // ------------------------------------

  // .............
  // TO COMPLETE
  // .............

  private void send_RTSP_request(String request_type) {
    try {
      RTSPBufferedWriter.write(request_type + " " + VideoFileName + " RTSP/1.0" + CRLF);
      RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);
      if (request_type.equals("SETUP")) {
        RTSPBufferedWriter.write("Transport: RTP/UDP; client_port= " + RTP_RCV_PORT + CRLF);
      } else {
        RTSPBufferedWriter.write("Session: " + RTSPid + CRLF);
      }
      RTSPBufferedWriter.flush();
    } catch (Exception ex) {
      System.out.println("Exception caught: " + ex);
      System.exit(0);
    }
  }

}