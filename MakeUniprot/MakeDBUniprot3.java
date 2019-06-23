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
  run on pfamseq.gz
  released with a given version of Pfam
*/
public class MakeDBUniprot3 {
    final public static void main(String argv[]) {
        try {
            // LocalSQL.connectRW();
            // PreparedStatement stmt1 = LocalSQL.prepareStatement("select id from uniprot where long_id=? and is_obsolete=0");
            // PreparedStatement stmt2 = LocalSQL.prepareStatement("insert into uniprot_seq values (?, ?)");
            // PreparedStatement stmt3 = LocalSQL.prepareStatement("delete from uniprot_seq where uniprot_id=?");

            ResultSet rs;

            BufferedReader proteins = IO.openReader(argv[0]);
            PolymerSet ps = new ProteinSet();
            Enumeration pe = ps.polymersInFile(proteins, null);
            while (pe.hasMoreElements()) {
                Polymer p;
                p = (Polymer)pe.nextElement();

                if ((p==null) || (p.name==null))
                    continue;

                // int pos1_ = p.name.indexOf(" ");
                // int pos2 = p.name.indexOf(" ",pos1+1);
                // String acc = p.name.substring(pos1+1,pos2);

                int pos1_blank = p.name.indexOf(" ");
                int pos1_line = p.name.indexOf("|");
                int pos2_line = p.name.indexOf("|",pos1_line+1);
                String acc = p.name.substring(pos2_line+1,pos1_blank);

                String seq = p.sequence().toLowerCase();
                System.out.println(acc);
                System.out.println(seq);



                // just store human seqs, for now:
                // if (acc.endsWith("_HUMAN")) {
                //     stmt1.setString(1,acc);
                //     rs = stmt1.executeQuery();
                //     if (rs.next()) {
                //         int uniprotID = rs.getInt(1);
                //         String seq = p.sequence().toLowerCase();
                //         int seqID = MakeDomainSeq.lookupOrCreateSeq(seq);
                //         stmt3.setInt(1,uniprotID);
                //         stmt3.executeUpdate();
                //
                //         stmt2.setInt(1,uniprotID);
                //         stmt2.setInt(2,seqID);
                //         stmt2.executeUpdate();
                //     }
                // }
            }
        }
        catch (Exception e) {
            System.out.println("Exception: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
