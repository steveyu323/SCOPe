package gov.lbl.scop.app;

import java.sql.*;
import java.io.*;
import java.util.*;
import java.text.*;
import org.strbio.IO;
import org.strbio.io.*;
import org.strbio.math.*;
import org.strbio.mol.*;
import org.strbio.util.*;
import gov.lbl.scop.local.LocalSQL;

/*
Updated by Changhua Yu: This one run only on uniprot_sprot.dat.gz and uniprot_trembl.dat.gz
and do not need a given version Pfam.
The code iterate through every single uniprot entry in uniprot_sprot.dat.gz
and then insert them into db if they are the missing ones from the missing_uniprot table with miss_type = 1
run on uniprot_sprot.dat.gz and uniprot_trembl.dat.gz
*/

public class ResolveInvalidUniprot2 {
  final public static SimpleDateFormat uniprotDateFormat =
  new SimpleDateFormat ("dd-MMM-yyyy");

  final public static void main(String argv[]) {
    try {
      LocalSQL.connectRW();
      Statement stmt = LocalSQL.createStatement();
      PreparedStatement stmt1 = LocalSQL.prepareStatement("update missing_acc set catched=1 where db_accesion=?");
      PreparedStatement stmt2 = LocalSQL.prepareStatement("insert into missing_match values (?, ?)");
      //PreparedStatement stmt5 = LocalSQL.prepareStatement("delete from uniprot_seq where uniprot_id=?");

      ResultSet rs;
      int id;
      //skip lines?
      long skipLines = 0;
      if (argv.length > 1)
      skipLines = StringUtil.atol(argv[0]);
      if (skipLines > 0)
      System.out.println("Skipping first "+skipLines+" records of file");

      boolean isSprot = (argv[0].indexOf("sprot") > -1);

      // if (skipLines==0)
      // stmt.executeUpdate("update uniprot set is_obsolete=1 where is_swissprot="+(isSprot ? 1 : 0));

      // parse in the long_id with miss_type = 1
      Set<String> acc_set = new HashSet<String>();
      ResultSet rs1;
      rs1 = stmt.executeQuery("select db_accesion from missing_acc where catched = 0");
      while (rs1.next()) {
        String acc = rs1.getString(1);
        acc_set.add(acc);
      }

      if (acc_set.contains("Q53Y18")) {
        System.out.println("have it!!!!!");
      }

      // add the sequences with miss_type = 1 into the uniprot table
      BufferedReader infile = IO.openReader(argv[0]);
      String longID= null, name=null, seq="";
      int seqVer = 0;
      java.util.Date seqDate = null;
      String[] accs = null;

      while (infile.ready()) {
        String buffer = infile.readLine().trim();

        if (buffer.startsWith("ID ")) {
          if (skipLines-- <= 0) {
            longID = buffer.substring(5);
            int pos = longID.indexOf(' ');
            longID = longID.substring(0,pos);
          }
        }
        else if ((buffer.startsWith("DT ")) &&
        (buffer.indexOf("sequence version")==18)) {
          if (skipLines <= 0) {
            seqVer = StringUtil.atoi(buffer,35);
            seqDate = uniprotDateFormat.parse(buffer.substring(5,16));
          }
        }
        else if ((buffer.startsWith("DE ")) &&
        ((buffer.indexOf("RecName: Full=")==5) ||
        (buffer.indexOf("SubName: Full=")==5))) {
          if (skipLines <= 0) {
            name = buffer.substring(19,buffer.length()-1);
          }
        }
        else if (buffer.startsWith("AC ")) {
          if (skipLines <= 0) {
            accs = buffer.substring(5).split(";");
          }
        }

        else if (buffer.startsWith("SQ ")) {
          while (!buffer.startsWith("//")) {
            buffer = infile.readLine().trim();
            if (!buffer.startsWith("//")){
              seq += StringUtil.replace(buffer," ","").toLowerCase();
            }
          }

          for (int i=0; i<accs.length; i++) {
            String acc = accs[i].trim();
            //For debug
            if (acc.equals("P02679")) {
              System.out.println(accs);
            }

            if (acc_set.contains(acc)) {
                //add to uniprot_accession table
                System.out.println("acc = '"+acc+"'");
                System.out.println("debug: update missing_match");
                stmt2.setString(1,acc);
                stmt2.setString(2,longID);
                stmt2.executeUpdate();

                // change the miss_type from 1 to 5
                System.out.println("debug: update missing_acc");
                stmt1.setString(1,acc);
                stmt1.executeUpdate();
            }
          }

          longID= null;
          name=null;
          seq="";
          seqVer = 0;
          seqDate = null;
          accs = null;
        }
      }
    } catch (Exception e) {
      System.out.println("Exception: "+e.getMessage());
      e.printStackTrace();
    }
  }

}
