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
import gov.lbl.scop.util.RAF;

/*
The function performs the following:
1) select the release_id with raf version = 18
2) for each of the release_id, identify the pdb_chain_id relating to it that is linked to the raf_version = 3
3) get the dbref entry of the above pdb_chain_id
4) get the alignment information from the pdb_chain_db ref, and substring using pdb_chain_dbref align_start and align_end
5) get the uniprot sequence from astral chain and the seq from raf entry
6) perform string comparison
7) Store in a new table compare_seq with columns:
(pdb_release_id INT NOT NULL,
pdb_chain_id INT NOT NULL,
raf_available Bool NOT NULL,
long_id TEXT NOT NULL,
uniprot_id INT NOT NULL,
seq_id INT NOT NULL,
is_same Bool NOT NULL)
*/

public class InspectCompare {
  final public static void compareUniSeqRes(int pdbReleaseID) throws Exception {
    System.out.println("current pdbReleaseID is " + pdbReleaseID);
    Statement stmt = LocalSQL.createStatement();
    PreparedStatement stmt1 = LocalSQL.prepareStatement("select id from uniprot where long_id = ? and is_obsolete = 0");
    PreparedStatement stmt2 = LocalSQL.prepareStatement("select seq_id from uniprot_seq where uniprot_id = ?");
    PreparedStatement stmt3 = LocalSQL.prepareStatement("select seq from astral_seq where id = ? and is_reject = 0");
    PreparedStatement stmt4 = LocalSQL.prepareStatement("insert into compare_seq values (?,?,?,?,?,?,?)");
    PreparedStatement stmt5 = LocalSQL.prepareStatement("select raf_get_body(id) from raf where pdb_chain_id = ? and first_release_id is null and last_release_id is null and raf_version_id = 3");


    ResultSet rs;
    ResultSet rs1;
    ResultSet rs2;
    ResultSet rs3;
    ResultSet rs5;

    int pdb_chain_id = -1;
    boolean raf_available = false;
    String db_code = "NOT AVAILABLE";
    int uniprot_id = -1;
    int seq_id = -1;
    boolean is_same = false;
    String astral_sequence = null;
    String raf_sequence = null;
    rs = stmt.executeQuery("select * from pdb_chain_dbref where db_name = 'UNP' and pdb_align_start is not null and pdb_align_end is not null and pdb_chain_id in (select pdb_chain_id from raf where pdb_chain_id in (select id from pdb_chain where pdb_release_id ="+pdbReleaseID + ") and raf_version_id = 3)");
    if (!rs.next()) {
      System.out.println("rs is empty");
      return;
    } else {
      System.out.println("rs is not empty");
      rs.previous();
      raf_available = true;
    }

    while (rs.next()) {
      pdb_chain_id = rs.getInt(2);
      System.out.println("pdb_chain_id = " + pdb_chain_id);
      db_code = rs.getString(4);

      int db_align_start = rs.getInt(6) - 1;
      int db_align_end = rs.getInt(7);

      if(db_align_start == -1) {
        db_align_start += 1;
        db_align_end += 1;
      }

      int pdb_align_start = rs.getInt(8);
      int pdb_align_end = rs.getInt(9);
      System.out.println(" db_align_start: " + db_align_start + " db_align_end: " + db_align_end + " pdb_align_start: " + pdb_align_start + " pdb_align_end: " + pdb_align_end );
      //System.out.println("pdb_chain_id = " + pdb_chain_id);
      //System.out.println("db_code = " + db_code);

      stmt5.setInt(1,pdb_chain_id);
      rs5 = stmt5.executeQuery();

      if (rs5.next()) {
        System.out.println("rs5 is not empty");
        raf_sequence = rs5.getString(1);
        System.out.println("get raf sequence !");
        System.out.println("raf_sequence = " + raf_sequence);
      }
      rs5.close();

      System.out.println("db_code = " + db_code);
      stmt1.setString(1,db_code);
      rs1 = stmt1.executeQuery();
      if (rs1.next()) {

        uniprot_id = rs1.getInt(1);
        rs1.close();
        System.out.println("get uniprot_id = " + uniprot_id);

        stmt2.setInt(1,uniprot_id);
        rs2 = stmt2.executeQuery();
        if (rs2.next()) {
          seq_id = rs2.getInt(1);
          rs2.close();
          System.out.println("get seq_id = " + seq_id);
          stmt3.setInt(1,seq_id);
          rs3 = stmt3.executeQuery();

          if (rs3.next()) {
            astral_sequence = rs3.getString(1);
            System.out.println("astral_sequence =  " + astral_sequence);
          }
          rs3.close();

          //System.out.println(astral_sequence.length());
          if ((astral_sequence != null)) {
            if ((astral_sequence.length() >= db_align_end)) {
              // compare raf_sequence and astral_sequence with proper indexing
              System.out.println(" db_align_start: " + db_align_start + " db_align_end: " + db_align_end + " pdb_align_start: " + pdb_align_start + " pdb_align_end: " + pdb_align_end );
              String temp_astral = astral_sequence.substring(db_align_start,db_align_end);
              String temp_raf = RAF.partialChainSeq(raf_sequence, 1, pdb_align_start, pdb_align_end).getSequence();
              System.out.println("seqres: " + temp_raf);
              System.out.println("astral: " + temp_astral);
              System.out.println("diff = " + temp_raf.replaceAll(temp_astral, "_"));
              is_same = temp_astral.equals(temp_raf);
            }
          }
        } else  {
          rs2.close();
        }
      } else {
        rs1.close();
      }
      uniprot_id = -1;
      seq_id = -1;
      is_same = false;
      raf_sequence = null;
      astral_sequence = null;
    }
    rs.close();
    stmt1.close();
    stmt2.close();
    stmt3.close();
    stmt.close();
  }



  final public static void main(String argv[]) {
    try {
        compareUniSeqRes(Integer.parseInt(argv[0]));
    } catch (Exception e) {
      System.out.println("Exception: "+e.getMessage());
      e.printStackTrace();
    }
  }
}
