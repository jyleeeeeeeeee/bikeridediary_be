package com.bikeridediary.infra.coordinates;

import org.locationtech.proj4j.*;
import org.springframework.stereotype.Component;

@Component
public class CoordinateConverter {
    private final CoordinateTransform katecToWgs84;
    private final CoordinateTransform wgs84ToKatec;

    public CoordinateConverter() {
        CRSFactory factory = new CRSFactory();
        CoordinateReferenceSystem katec = factory.createFromParameters("KATEC",
                "+proj=tmerc +lat_0=38 +lon_0=128 +k=0.9999 " +
                        "+x_0=400000 +y_0=600000 +ellps=bessel +units=m +no_defs " +
                        "+towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43");
        CoordinateReferenceSystem wgs84 = factory.createFromParameters("WGS84",
                "+proj=longlat +datum=WGS84 +no_defs");

        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        this.katecToWgs84 = ctFactory.createTransform(katec, wgs84);
        this.wgs84ToKatec = ctFactory.createTransform(wgs84, katec);
    }

    // 지도 상 위경도 -> 오피넷 파라미터 형식 좌표(KATEC)
    public double[] toKatec(double lat, double lng) {
        ProjCoordinate src = new ProjCoordinate(lng, lat);// x = 위도, y = 경도
        ProjCoordinate tgt = new ProjCoordinate();

        wgs84ToKatec.transform(src, tgt);
        return new double[]{tgt.x, tgt.y};
    }

    // 오피넷 응답 좌표(KATEC) -> 지도 상 위경도
    public double[] toWgs84(double katecX, double katecY) {
        ProjCoordinate src = new ProjCoordinate(katecX, katecY);// x = 위도, y = 경도
        ProjCoordinate tgt = new ProjCoordinate();

        katecToWgs84.transform(src, tgt);
        return new double[]{tgt.y, tgt.x};
    }




}
