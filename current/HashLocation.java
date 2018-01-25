import bc.*;
public class HashLocation {

	private MapLocation maploc;
	public HashLocation(MapLocation h){
		maploc = h;
	}
	private MapLocation getMapLocation(){
		return maploc;
	}
	
	@Override
	public boolean equals(Object o){
		return this.maploc.equals(((HashLocation)o).getMapLocation());
	}

	@Override
	public int hashCode()
    {
		int code;
		if(maploc.getPlanet()==Planet.Earth){
			code = maploc.getX()+maploc.getY()*100;
		}
		else{
			code = maploc.getX()+maploc.getY()*100+10000;
		}
        return code;
    }
}
