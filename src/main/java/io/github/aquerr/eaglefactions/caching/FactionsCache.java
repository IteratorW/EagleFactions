package io.github.aquerr.eaglefactions.caching;

import io.github.aquerr.eaglefactions.entities.Claim;
import io.github.aquerr.eaglefactions.entities.Faction;

import javax.annotation.Nullable;
import java.util.*;

public class FactionsCache
{
    private static final Map<String, Faction> factionsCacheMap = new HashMap<>();
    private static final Set<Claim> claimsCacheSet = new HashSet<>();
//    private static final Map<String, Set<String>> factionsClaimsCache = new Hashtable<>();

    private FactionsCache()
    {

    }

    public static Map<String, Faction> getFactionsMap()
    {
        return factionsCacheMap;
    }

    public static void addOrUpdateFactionCache(Faction faction)
    {
        synchronized (factionsCacheMap)
        {
            Faction factionToUpdate = factionsCacheMap.get(faction.getName().toLowerCase());

            if (factionToUpdate != null)
            {
                factionsCacheMap.replace(factionToUpdate.getName().toLowerCase(), faction);
                claimsCacheSet.removeAll(factionToUpdate.getClaims());
            }
            else
            {
                factionsCacheMap.put(faction.getName().toLowerCase(), faction);
            }

            if(!faction.getClaims().isEmpty())
            {
                claimsCacheSet.addAll(faction.getClaims());
            }
        }
    }

    public static void removeFactionCache(String factionName)
    {
        synchronized (factionsCacheMap)
        {
            Faction faction = factionsCacheMap.remove(factionName.toLowerCase());
            claimsCacheSet.removeAll(faction.getClaims());
        }
    }

    @Nullable
    public static Faction getFactionCache(String factionName)
    {
        Faction optionalFaction = factionsCacheMap.get(factionName.toLowerCase());

        if (optionalFaction != null)
        {
            return optionalFaction;
        }

        return null;
    }

    public static Set<Claim> getAllClaims()
    {
        return claimsCacheSet;
    }

    public static void removeClaimCache(Claim claim)
    {
        claimsCacheSet.remove(claim);
    }

    public static void clearCache()
    {
        claimsCacheSet.clear();
        factionsCacheMap.clear();
    }
}
