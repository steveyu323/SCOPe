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

public class CompareUniprotSeqres {
  final public static void compareUniSeqRes(int pdbReleaseID) throws Exception {
    Statement stmt = LocalSQL.createStatement();
    PreparedStatement stmt1 = LocalSQL.prepareStatement("select id from uniprot where long_id = ? and is_obsolete = 0");
    PreparedStatement stmt2 = LocalSQL.prepareStatement("select seq_id from uniprot_seq where uniprot_id = ?");
    PreparedStatement stmt3 = LocalSQL.prepareStatement("select seq from astral_seq where id = ? and is_reject = 0");
    PreparedStatement stmt4 = LocalSQL.prepareStatement("insert into compare_seq values (?,?,?,?,?,?,?)");
    PreparedStatement stmt5 = LocalSQL.prepareStatement("select raf_get_body(id) from raf where pdb_chain_id= ? and first_release_id is null and last_release_id is null and raf_version_id = 3");


    ResultSet rs;
    ResultSet rs1;
    ResultSet rs2;
    ResultSet rs3;
    ResultSet rs5;

    int pdb_chain_id = -1;
    Bool raf_available = false;
    String db_code = "NOT AVAILABLE";
    int uniprot_id = -1;
    int seq_id = -1;
    Bool is_same = false;
    String astral_sequence;

    rs = stmt.executeQuery("select * from pdb_chain_dbref where db_name = 'UNP' and pdb_chain_id in (select pdb_chain_id from raf where pdb_chain_id in (select id from pdb_chain where pdb_release_id ="+pdbReleaseID + ") and raf_version_id = 3)");
    if (!rs.next()) {
      stmt4.setInt(1,pdbReleaseID);
      stmt4.setInt(2,pdb_chain_id);
      stmt4.setInt(3,raf_available);
      stmt4.setString(4,db_code);
      stmt4.setInt(5,uniprot_id);
      stmt4.setInt(6,seq_id);
      stmt4.setInt(7,is_same);
      stmt4.executeUpdate();
      stmt.close();
      return;
    } else {
      raf_available = true;
    }

    while (rs.next()) {
      pdb_chain_id = rs.getInt(2);
      db_code = rs.getString(4);
      int db_align_start = rs.getInt(6) - 1;
      int db_align_end = rs.getInt(7);
      int pdb_align_start = rs.getInt(8);
      int pdb_align_end = rs.getInt(9) + 1;

      stmt5.setInt(1,pdb_chain_id);
      rs5 = stmt5.executeQuery();
      String raf_sequence = rs5.getString(1);

      stmt1.setString(1,db_code);
      rs1 = stmt1.executeQuery();
      if (!rs1.next()) {
        uniprot_id = rs1.getInt(1);

        stmt2.setInt(1,uniprot_id);
        rs2 = stmt2.executeQuery();
        if (!rs2.next()) {
          seq_id = rs2.getInt(1);

          stmt3.setInt(1,seq_id);
          rs3 = stmt3.executeQuery();

          if (!rs3.next()) {
            astral_sequence = rs3.getString(1);
          }

          if (astral_sequence != null) {
            // compare raf_sequence and astral_sequence with proper indexing
            String temp_astral = astral_sequence.substring(db_align_start,db_align_end);
            String temp_raf = raf_sequence.substring(pdb_align_start,pdb_align_end);
            is_same = temp_astral.equals(temp_raf);
            stmt4.setInt(1,pdbReleaseID);
            stmt4.setInt(2,pdb_chain_id);
            stmt4.setInt(3,raf_available);
            stmt4.setString(4,db_code);
            stmt4.setInt(5,uniprot_id);
            stmt4.setInt(6,seq_id);
            stmt4.setInt(7,is_same);
            stmt4.executeUpdate();
          }
        }
      }
      uniprot_id = -1;
      seq_id = -1;
      is_same = false;
    }

    stmt1.close();
    stmt2.close();
    stmt3.close();
    stmt.close();

  }



  final public static void main(String argv[]) {
    try {
      LocalSQL.connectRW();
      Statement stmt = LocalSQL.createStatement();

      ResultSet rs;
      rs = stmt.executeQuery("select pdb_release_id from pdb_local where xml_path is not null and pdb_release_id in (select distinct(pdb_release_id) from pdb_chain where id in (select pdb_chain_id from raf where last_release_id = 18))");
      while (rs.next()) {
        int id = rs.getInt(1);
        compareUniSeqRes(id);
      }
    } catch (Exception e) {
      System.out.println("Exception: "+e.getMessage());
      e.printStackTrace();
    }
  }

}
