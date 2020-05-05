package craftedMods.lotr.mpc.data.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.osgi.service.component.annotations.*;

import craftedMods.lotr.mpc.data.api.MusicPackData;

@Component
public class MusicPackDataImpl implements MusicPackData
{

    private Map<String, Collection<String>> regions;
    private List<String> categories;

    @Activate
    public void onActivate ()
    {
        this.initRegions ();
        this.initCategories ();
    }

    private void initRegions ()
    {
        this.regions = new TreeMap<> ();

        this.regions.put ("all", Arrays.asList ());
        this.regions.put ("menu", Arrays.asList ());
        this.regions.put ("sea", Arrays.asList ("sea", "meneltarma", "lake"));
        this.regions.put ("shire", Arrays.asList ("shire", "woodland", "whiteDowns", "moors"));
        this.regions.put ("oldForest", Arrays.asList ("oldForest"));
        this.regions.put ("lindon", Arrays.asList ("lindon", "towerHills"));
        this.regions.put ("barrowDowns", Arrays.asList ("barrowDowns"));
        this.regions.put ("bree", Arrays.asList ("bree", "chetwood"));
        this.regions.put ("eriador",
            Arrays.asList ("trollshaws", "loneLands", "angle", "eriador", "midgewater", "swanfleet", "minhiriath"));
        this.regions.put ("rivendell", Arrays.asList ("rivendell"));
        this.regions.put ("angmar", Arrays.asList ("ettenmoors", "angmar", "coldfells"));
        this.regions.put ("eregion", Arrays.asList ("eregion"));
        this.regions.put ("enedwaith", Arrays.asList ("enedwaith"));
        this.regions.put ("dunland", Arrays.asList ("dunland", "adorn"));
        this.regions.put ("pukel", Arrays.asList ("andrast", "pukel"));
        this.regions.put ("mistyMountains", Arrays.asList ("mistyMountains"));
        this.regions.put ("forodwaith", Arrays.asList ("forodwaith", "tundra"));
        this.regions.put ("greyMountains", Arrays.asList ("greyMountains"));
        this.regions.put ("rhovanion", Arrays.asList ("celebrant", "anduin", "gladden", "wilderland", "longMarshes"));
        this.regions.put ("mirkwood", Arrays.asList ("mirkwood", "north", "dolGuldur"));
        this.regions.put ("woodlandRealm", Arrays.asList ("woodlandRealm"));
        this.regions.put ("dale", Arrays.asList ("dale"));
        this.regions.put ("dwarven", Arrays.asList ("ironHills", "blueMountains", "erebor", "redMountains"));
        this.regions.put ("lothlorien", Arrays.asList ("lothlorien", "edge"));
        this.regions.put ("fangorn", Arrays.asList ("fangorn"));
        this.regions.put ("rohan", Arrays.asList ("rohan", "wold"));
        this.regions.put ("isengard", Arrays.asList ("rohan", "fangorn", "isengard"));
        this.regions.put ("gondor", Arrays.asList ("gondor", "whiteMountains", "ithilien", "pelargir", "lebennin",
            "dolAmroth", "pelennor", "pinnathGelin", "tolfalas", "lossarnach", "lamedon", "blackroot"));
        this.regions.put ("brownLands", Arrays.asList ("emynMuil", "brownLands"));
        this.regions.put ("deadMarshes", Arrays.asList ("deadMarshes", "nindalf"));
        this.regions.put ("mordor",
            Arrays.asList ("mordor", "mountains", "dagorlad", "nurn", "nanUngol", "morgulVale", "east"));
        this.regions.put ("rhun", Arrays.asList ("rhun", "rhudel", "lastDesert", "windMountains"));
        this.regions.put ("dorwinion", Arrays.asList ("dorwinion"));
        this.regions.put ("nearHarad", Arrays.asList ("harondor", "desert", "umbar", "lostladen", "fertile", "gulf", "oasis", "harnedor"));
        this.regions.put ("farHarad",
            Arrays.asList ("savannah", "mountains", "swamp", "bushland", "mangrove", "volcano", "kanuka"));
        this.regions.put ("farHaradJungle", Arrays.asList ("jungle", "edge", "cloudForest"));
        this.regions.put ("pertorogwaith", Arrays.asList ("pertorogwaith"));
        this.regions.put ("utumno", Arrays.asList ("utumno"));

        this.regions = Collections.unmodifiableMap (this.regions);
    }

    private void initCategories ()
    {
        this.categories = new ArrayList<> ();

        this.categories.add ("day");
        this.categories.add ("night");
        this.categories.add ("cave");

        this.categories = Collections.unmodifiableList (categories);
    }

    @Override
    public String getLOTRModVersion ()
    {
        return "Beta 35.4";
    }

    @Override
    public Map<String, Collection<String>> getRegions ()
    {
        return regions;
    }

    @Override
    public String getDefaultRegion ()
    {
        return "all";
    }

    @Override
    public Collection<String> getCategories ()
    {
        return categories;
    }

}
