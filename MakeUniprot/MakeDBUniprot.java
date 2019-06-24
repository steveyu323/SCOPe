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

public class MakeDBUniprot {
  final public static SimpleDateFormat uniprotDateFormat =
  new SimpleDateFormat ("dd-MMM-yyyy");

  final public static void main(String argv[]) {
    try {
      LocalSQL.connectRW();
      Statement stmt = LocalSQL.createStatement();
      PreparedStatement stmt1 = LocalSQL.prepareStatement("update missing_uniprot set miss_type=4 where long_id=?");
      PreparedStatement stmt2 = LocalSQL.prepareStatement("insert into uniprot values (null, ?, ?, ?, ?, ?, 0)",
      Statement.RETURN_GENERATED_KEYS);
      PreparedStatement stmt3 = LocalSQL.prepareStatement("insert into uniprot_accession values (?, ?)");
      PreparedStatement stmt4 = LocalSQL.prepareStatement("insert into uniprot_seq values (?, ?)");
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
      Set<String> id_set = new HashSet<String>();
      ResultSet rs1;
      rs1 = stmt.executeQuery("select long_id from missing_uniprot where miss_type = 1");
      while (rs1.next()) {
        String acc = rs1.getString(1);
        id_set.add(acc);
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

          if (id_set.contains(longID)) {
            int seqID = MakeDomainSeq.lookupOrCreateSeq(seq);

            System.out.println("id = '"+longID+"'");
            System.out.println("seqVer = '"+seqVer+"'");
            System.out.println("seqDate = '"+ParsePDB.sqlDateFormat.format(seqDate)+"'");
            System.out.println("name = '"+name+"'");
            for (int i=0; i<accs.length; i++)
            System.out.println("acc = '"+accs[i].trim()+"'");
            System.out.println("seq = '"+seq+"'");
            System.out.println("seqID = '"+seqID+"'");

            // add to uniprot table
            if (skipLines <= 0) {
              stmt2.setString(1,longID);
              stmt2.setInt(2,isSprot?1:0);
              stmt2.setInt(3,seqVer);
              stmt2.setDate(4,new java.sql.Date(seqDate.getTime()));
              stmt2.setString(5,name);
              System.out.println("debug: update uniprot");
              stmt2.executeUpdate();
              rs = stmt2.getGeneratedKeys();
              rs.next();
              id = rs.getInt(1);
              rs.close();

              // update the uniprot_seq table as well
              System.out.println("debug: update uniprot_seq");
              stmt4.setInt(1,id);
              stmt4.setInt(2,seqID);
              stmt4.executeUpdate();

              //add to uniprot_accession table
              System.out.println("debug: update uniprot_accession");
              stmt.executeUpdate("delete from uniprot_accession where uniprot_id="+id);
              stmt3.setInt(1,id);
              for (int i=0; i<accs.length; i++) {
                stmt3.setString(2,accs[i].trim());
                stmt3.executeUpdate();
              }

              // change the miss_type from 1 to 4
              System.out.println("debug: update missing_uniprot");
              stmt1.setString(1,longID);
              stmt1.executeUpdate();

              longID= null;
              name=null;
              seq="";
              seqVer = 0;
              seqDate = null;
              accs = null;
            }
          }
        }
      }
    } catch (Exception e) {
      System.out.println("Exception: "+e.getMessage());
      e.printStackTrace();
    }
  }

}
