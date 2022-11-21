package Prettifier;

import CommitConsumers.CommitCollector;
import CommitModifiers.CommitIdentityMapper;

import java.util.*;

public interface Authors {
    static void handle(RepositoryWrapper rw) {
        CommitCollector cc = rw.visitCommits(new CommitCollector());
        List<Identity> ids = new ArrayList<>(cc.collectIdents());

        rw.rebuild(new CommitIdentityMapper(getMap(ids)));
    }

    static Map<Identity, Identity> getMap(List<Identity> ids) {
        Map<Identity, Identity> mp = new HashMap<>();
        for (Identity id : ids)
            mp.put(id, id);

        StringBuilder vimText = new StringBuilder();
        vimText.append("# Here are all identities in the format:\n");
        vimText.append("# {id}. {name} <{email}>\n");
        vimText.append("# You can map identities by appending \"-> {to id}\" to some line\n");
        vimText.append("\n");

        for (int i = 0; i < ids.size(); ++i)
            vimText.append("%d. %s <%s>\n".formatted(i+1, ids.get(i).name(), ids.get(i).emailAddress()));
        vimText.append("\n");

        Map<Integer, Identity> newIdMap = new HashMap<>();
        Map<Integer, Integer> mappings = new HashMap<>();

        String out = Main.launchVim(vimText.toString());
        for (String line : out.split("\n")) {
            line = line.strip();
            if (line.isEmpty() || line.charAt(0) == '#')
                continue;

            try {
                String[] splitted = line.split("\\.", 2);
                int id = Integer.parseInt(splitted[0].strip());

                splitted = splitted[1].split("<", 2);
                String name = splitted[0].strip();

                splitted = splitted[1].split(">", 2);
                String email = splitted[0].strip();

                newIdMap.put(id, new Identity(name, email));

                line = splitted[1].strip().substring(2).strip();
                int mapping = Integer.parseInt(line);

                mappings.put(id, mapping);

            } catch (RuntimeException ignored) {
            }
        }

        for (var entry : mappings.entrySet()) {
            try {
                int from_i = entry.getKey();
                int to_i = entry.getValue();

                Identity from = Objects.requireNonNull(newIdMap.get(from_i));
                Identity to = Objects.requireNonNull(newIdMap.get(to_i));

                mp.put(from, to);
            } catch (RuntimeException ignored) {
            }
        }

//        for (var entry : mp.entrySet())
//            System.out.println(entry);

        return mp;
    }
}
