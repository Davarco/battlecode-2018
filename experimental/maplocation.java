import bc.*;
public class maplocation {
	private MapLocation maploc=null;
	public maplocation(MapLocation h){
		maploc = h;
	}
	private MapLocation getmaplocation(){
		return maploc;
	}
	
	@Override
	public boolean equals(Object o){
		return this.maploc.equals(((maplocation)o).getmaplocation());
	}
	
	
	@Override
	public int hashCode()
    {
		int code = 0;
		if(maploc.getPlanet()==Planet.Earth){
			code = maploc.getX()+maploc.getY()*100;
		}
		else{
			code = maploc.getX()+maploc.getY()*100+10000;
		}
        return code;
    }
}
