--
-- PostgreSQL database dump
--

-- Dumped from database version 14.10 (Ubuntu 14.10-0ubuntu0.22.04.1)
-- Dumped by pg_dump version 14.10 (Ubuntu 14.10-0ubuntu0.22.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: uninadelivery; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA uninadelivery;


ALTER SCHEMA uninadelivery OWNER TO postgres;

--
-- Name: enum_stato; Type: TYPE; Schema: uninadelivery; Owner: postgres
--

CREATE TYPE uninadelivery.enum_stato AS ENUM (
    'In elaborazione',
    'Annullato',
    'Confermato',
    'Spedito',
    'Ricevuto'
);


ALTER TYPE uninadelivery.enum_stato OWNER TO postgres;

--
-- Name: acquirente_eliminato(); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.acquirente_eliminato() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	UPDATE uninadelivery.ORDINE as O SET emailacquirente = null WHERE O.emailacquirente = OLD.email;
	UPDATE uninadelivery.ORDINE as O SET stato = 'Annullato' WHERE O.emailacquirente = OLD.email AND O.stato <> 'Ricevuto';
	RETURN OLD;
END;
$$;


ALTER FUNCTION uninadelivery.acquirente_eliminato() OWNER TO postgres;

--
-- Name: annulla_ordine(); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.annulla_ordine() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	dettagli uninadelivery.DETTAGLI_ORDINE%ROWTYPE;
BEGIN
	IF NEW.stato = 'Annullato' THEN
		FOR dettagli IN (SELECT *
					FROM uninadelivery.DETTAGLI_ORDINE AS D
					WHERE D.idordine = NEW.idordine
		) LOOP
			UPDATE uninadelivery.DISPONIBILITà AS D SET quantità = quantità + dettagli.quantità
				WHERE D.idprodotto = dettagli.idprodotto AND D.idsede = NEW.idsede;
		END LOOP;
	END IF;
	RETURN NEW;
END;
$$;


ALTER FUNCTION uninadelivery.annulla_ordine() OWNER TO postgres;

--
-- Name: autenticazione_operatore(character varying, character varying); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.autenticazione_operatore(email_op character varying, passsword_op character varying) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN EXISTS(
		SELECT *
		FROM uninadelivery.OPERATORE AS O
		WHERE O.email = email_op AND O.password = passsword_op AND attivo
	);
END;
$$;


ALTER FUNCTION uninadelivery.autenticazione_operatore(email_op character varying, passsword_op character varying) OWNER TO postgres;

--
-- Name: conferma_ordine(integer); Type: PROCEDURE; Schema: uninadelivery; Owner: postgres
--

CREATE PROCEDURE uninadelivery.conferma_ordine(IN idordine_in integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF EXISTS(SELECT * FROM uninadelivery.ordine WHERE idordine = idordine_in AND stato = 'In elaborazione')
		AND EXISTS(SELECT * FROM uninadelivery.dettagli_ordine WHERE idordine = idordine_in) THEN
		UPDATE uninadelivery.ordine as O SET stato = 'Confermato' WHERE idordine = idordine_in;
	ELSE
		RAISE NOTICE 'ordine non trovato, vuoto o in uno stato diverso da quello di elaborazione';
	END IF;
END;
$$;


ALTER PROCEDURE uninadelivery.conferma_ordine(IN idordine_in integer) OWNER TO postgres;

--
-- Name: controlla_idsede_spedizione(); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.controlla_idsede_spedizione() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    idsedeordine uninadelivery.SEDE.idsede%TYPE;
    idsedeoperatore uninadelivery.SEDE.idsede%TYPE;
    idsedecorriere uninadelivery.SEDE.idsede%TYPE;
    idsedemezzo uninadelivery.SEDE.idsede%TYPE;
BEGIN
    SELECT idsede INTO idsedeoperatore FROM uninadelivery.OPERATORE AS O WHERE O.codicefiscale = NEW.codicefiscaleoperatore;
    SELECT idsede INTO idsedecorriere FROM uninadelivery.CORRIERE AS C WHERE C.codicefiscale = NEW.codicefiscalecorriere;
    SELECT idsede INTO idsedemezzo FROM uninadelivery.MEZZO_DI_TRASPORTO AS M WHERE M.targa = NEW.targa;

    IF idsedemezzo = idsedecorriere AND idsedecorriere = idsedeoperatore AND NOT EXISTS(
        SELECT * FROM uninadelivery.ORDINE AS O
        WHERE O.idspedizione = NEW.idspedizione AND O.idsede <> idsedeoperatore
    ) THEN
        RETURN NEW;
    ELSE
        RAISE 'Ad una spedizione non possono essere associati ordini, operatore, corriere o mezzo con sede diversa tra loro.';
        RETURN OLD;
    END IF;
END;
$$;


ALTER FUNCTION uninadelivery.controlla_idsede_spedizione() OWNER TO postgres;

--
-- Name: controlla_intervalli_spedizione(); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.controlla_intervalli_spedizione() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    puo_guidare BOOLEAN;
BEGIN
    puo_guidare := uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(NEW.codicefiscalecorriere,NEW.targa);

    IF (uninadelivery.is_corriere_disponibile(NEW.codicefiscalecorriere,NEW.partenza,NEW.arrivostimato) OR (OLD.codicefiscalecorriere = NEW.codicefiscalecorriere)) AND
            (uninadelivery.is_mezzo_di_trasporto_disponibile(NEW.targa,NEW.partenza,NEW.arrivostimato) OR (OLD.targa = NEW.targa)) AND puo_guidare THEN
            RETURN NEW;
    ELSEIF puo_guidare THEN
        RAISE 'Il corriere o il mezzo selezionati non sono disponibili in questo orario';
    ELSE
        RAISE 'la patente del corriere selezionato non è adatta al mezzo di trasporto selezionato.';
    END IF;
    RETURN OLD;
END;
$$;


ALTER FUNCTION uninadelivery.controlla_intervalli_spedizione() OWNER TO postgres;

--
-- Name: controlla_ordine_spedito(); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.controlla_ordine_spedito() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF NEW.idspedizione IS null AND NEW.stato = 'Spedito' THEN
		RAISE 'non è possibile impostare lo stato di un ordine a spedito se non gli sono associate spedizioni';
		RETURN OLD;
	ELSEIF NEW.idspedizione IS NOT null AND (NEW.stato <> 'Confermato' OR NOT EXISTS(
		SELECT *
		FROM uninadelivery.DETTAGLI_ORDINE as D
		WHERE D.idordine = NEW.idordine
	)) THEN
		RAISE 'Si tenta di spedire un ordine vuoto o in elaborazione';
		RETURN OLD;
	END IF;
	RETURN NEW;
END;
$$;


ALTER FUNCTION uninadelivery.controlla_ordine_spedito() OWNER TO postgres;

--
-- Name: controllo_capienza_e_tempi_ordine_aggiunto_a_spedizione(); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.controllo_capienza_e_tempi_ordine_aggiunto_a_spedizione() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  peso_totale uninadelivery.PRODOTTO.peso%TYPE;
  peso_ordine uninadelivery.PRODOTTO.peso%TYPE;
  arrivostimato uninadelivery.SPEDIZIONE.arrivostimato%TYPE;
  partenza uninadelivery.SPEDIZIONE.partenza%TYPE;
  targamezzo uninadelivery.MEZZO_DI_TRASPORTO.targa%TYPE;
BEGIN
  -- esci se spedizione null
  IF NEW.idspedizione IS NULL THEN
    RETURN NEW;
  END IF;

  SELECT S.arrivostimato, S.partenza, S.targa INTO arrivostimato, partenza,targamezzo
  FROM uninadelivery.SPEDIZIONE AS S
  WHERE S.idspedizione = NEW.idspedizione;

  -- calcola il peso totale per la spedizione
  SELECT SUM(P.peso * D.quantità) INTO peso_totale
  FROM (uninadelivery.ORDINE AS O JOIN uninadelivery.DETTAGLI_ORDINE AS D ON O.idordine = D.idordine)
      JOIN uninadelivery.PRODOTTO AS P ON P.idprodotto = D.idprodotto
  WHERE O.idspedizione = NEW.idspedizione;

  SELECT SUM(P.peso * D.quantità) INTO peso_ordine
  FROM (uninadelivery.ORDINE AS O JOIN uninadelivery.DETTAGLI_ORDINE AS D ON O.idordine = D.idordine)
      JOIN uninadelivery.PRODOTTO AS P ON P.idprodotto = D.idprodotto
  WHERE O.idordine = NEW.idordine;

  peso_totale := peso_totale + peso_ordine;

  -- controlla se è stata superata la capienza massima del mezzo (raise exception)
  IF peso_totale > (SELECT capienza FROM uninadelivery.MEZZO_DI_TRASPORTO AS M WHERE M.targa = targamezzo) THEN
    RAISE 'In seguito all''inserimento o all''aggiornamento dell''ordine, è stata superata la capienza massima del mezzo di trasporto selezionato.';
    RETURN OLD;
  END IF;
  IF NEW.data + NEW.orarioinizio > arrivostimato OR NEW.data + NEW.orariofine < partenza THEN
    RAISE NOTICE 'Attenzione, l''ordine è stato inserito in una spedizione che non lo consegnerà in orario, avvisare il destinatario.';
  END IF;
  RETURN NEW;
END;
$$;


ALTER FUNCTION uninadelivery.controllo_capienza_e_tempi_ordine_aggiunto_a_spedizione() OWNER TO postgres;

--
-- Name: corriere_puo_guidare_mezzo_di_trasporto(character, character); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(codicefiscalecorriere character, targamezzo character) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	patente_corriere uninadelivery.CORRIERE.tipopatente%TYPE;
	patente_richiesta uninadelivery.MEZZO_DI_TRASPORTO.patenterichiesta%TYPE;
	corriere_attivo BOOLEAN;
	mezzo_attivo BOOLEAN;
BEGIN
	SELECT tipopatente, attivo INTO patente_corriere, corriere_attivo
	FROM uninadelivery.CORRIERE AS C
	WHERE C.codicefiscale = codicefiscalecorriere;
	
	SELECT patenterichiesta, attivo INTO patente_richiesta, mezzo_attivo
	FROM uninadelivery.MEZZO_DI_TRASPORTO AS M
	WHERE M.targa = targamezzo;
	
	RETURN (corriere_attivo = true AND mezzo_attivo = true) AND (
			patente_corriere = 'C' OR patente_corriere = 'C1' OR patente_corriere = 'CE' OR
			patente_corriere = 'BE' AND patente_richiesta <> 'C' AND patente_richiesta <> 'B96' OR
			patente_corriere = 'B' AND patente_richiesta = 'B' OR
			patente_corriere = 'B96' AND patente_richiesta <> 'C'
	);
END;
$$;


ALTER FUNCTION uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(codicefiscalecorriere character, targamezzo character) OWNER TO postgres;

--
-- Name: corriere_puo_guidare_mezzo_di_trasporto_stessa_sede(character, character); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.corriere_puo_guidare_mezzo_di_trasporto_stessa_sede(codicefiscalecorriere character, targamezzo character) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	sede_corriere uninadelivery.CORRIERE.idsede%TYPE;
	sede_mezzo uninadelivery.MEZZO_DI_TRASPORTO.idsede%TYPE;
	richiesta BOOLEAN;
BEGIN
	SELECT idsede INTO sede_corriere
	FROM uninadelivery.CORRIERE AS C
	WHERE C.codicefiscale = codicefiscalecorriere;
	
	SELECT idsede INTO sede_mezzo
	FROM uninadelivery.MEZZO_DI_TRASPORTO AS M
	WHERE M.targa = targamezzo;
	
	RETURN uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(codicefiscalecorriere, targamezzo) AND sede_corriere = sede_mezzo;
END;
$$;


ALTER FUNCTION uninadelivery.corriere_puo_guidare_mezzo_di_trasporto_stessa_sede(codicefiscalecorriere character, targamezzo character) OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: corriere; Type: TABLE; Schema: uninadelivery; Owner: postgres
--

CREATE TABLE uninadelivery.corriere (
    codicefiscale character(16) NOT NULL,
    nome character varying NOT NULL,
    cognome character varying NOT NULL,
    salario numeric NOT NULL,
    tipopatente character varying NOT NULL,
    idsede integer NOT NULL,
    attivo boolean DEFAULT true NOT NULL,
    CONSTRAINT codice_fiscale_valido CHECK ((codicefiscale ~ similar_to_escape('[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]'::text))),
    CONSTRAINT salario_positivo CHECK ((salario > (0)::numeric))
);


ALTER TABLE uninadelivery.corriere OWNER TO postgres;

--
-- Name: get_corrieri_disponibili(timestamp without time zone, timestamp without time zone); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.get_corrieri_disponibili(inizio timestamp without time zone, fine timestamp without time zone) RETURNS SETOF uninadelivery.corriere
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY (SELECT *
		FROM uninadelivery.CORRIERE AS C
		WHERE uninadelivery.is_corriere_disponibile(C.codicefiscale, inizio, fine));
END;
$$;


ALTER FUNCTION uninadelivery.get_corrieri_disponibili(inizio timestamp without time zone, fine timestamp without time zone) OWNER TO postgres;

--
-- Name: get_corrieri_disponibili_con_mezzo_di_trasporto(timestamp without time zone, timestamp without time zone, character); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.get_corrieri_disponibili_con_mezzo_di_trasporto(inizio timestamp without time zone, fine timestamp without time zone, targa character) RETURNS SETOF uninadelivery.corriere
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY SELECT * FROM uninadelivery.get_corrieri_disponibili(inizio, fine) INTERSECT
			SELECT * FROM uninadelivery.get_corrieri_per_mezzo_di_trasporto(targa);
END;
$$;


ALTER FUNCTION uninadelivery.get_corrieri_disponibili_con_mezzo_di_trasporto(inizio timestamp without time zone, fine timestamp without time zone, targa character) OWNER TO postgres;

--
-- Name: get_corrieri_per_mezzo_di_trasporto(character); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.get_corrieri_per_mezzo_di_trasporto(targa character) RETURNS SETOF uninadelivery.corriere
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
		SELECT *
		FROM uninadelivery.CORRIERE AS C
		WHERE uninadelivery.corriere_puo_guidare_mezzo_di_trasporto_stessa_sede(C.codicefiscale, targa);
END;
$$;


ALTER FUNCTION uninadelivery.get_corrieri_per_mezzo_di_trasporto(targa character) OWNER TO postgres;

--
-- Name: mezzo_di_trasporto; Type: TABLE; Schema: uninadelivery; Owner: postgres
--

CREATE TABLE uninadelivery.mezzo_di_trasporto (
    targa character(7) NOT NULL,
    tipomezzo character varying NOT NULL,
    patenterichiesta character varying NOT NULL,
    capienza double precision NOT NULL,
    idsede integer NOT NULL,
    attivo boolean DEFAULT true NOT NULL,
    CONSTRAINT targa_valida CHECK ((targa ~ similar_to_escape('[A-Z][A-Z][0-9][0-9][0-9][A-Z][A-Z]'::text)))
);


ALTER TABLE uninadelivery.mezzo_di_trasporto OWNER TO postgres;

--
-- Name: get_mezzi_di_trasporto_disponibili(timestamp without time zone, timestamp without time zone); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.get_mezzi_di_trasporto_disponibili(inizio timestamp without time zone, fine timestamp without time zone) RETURNS SETOF uninadelivery.mezzo_di_trasporto
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY SELECT *
		FROM uninadelivery.MEZZO_DI_TRASPORTO AS M
		WHERE uninadelivery.is_mezzo_di_trasporto_disponibile(M.targa, inizio, fine);
END;
$$;


ALTER FUNCTION uninadelivery.get_mezzi_di_trasporto_disponibili(inizio timestamp without time zone, fine timestamp without time zone) OWNER TO postgres;

--
-- Name: get_mezzi_di_trasporto_disponibili_con_corriere(timestamp without time zone, timestamp without time zone, character); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.get_mezzi_di_trasporto_disponibili_con_corriere(inizio timestamp without time zone, fine timestamp without time zone, codicefiscalecorriere character) RETURNS SETOF uninadelivery.mezzo_di_trasporto
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY SELECT * FROM uninadelivery.get_mezzi_di_trasporto_disponibili(inizio, fine) INTERSECT
			SELECT * FROM uninadelivery.get_mezzi_di_trasporto_per_corriere(codicefiscalecorriere);
END;
$$;


ALTER FUNCTION uninadelivery.get_mezzi_di_trasporto_disponibili_con_corriere(inizio timestamp without time zone, fine timestamp without time zone, codicefiscalecorriere character) OWNER TO postgres;

--
-- Name: get_mezzi_di_trasporto_per_corriere(character); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.get_mezzi_di_trasporto_per_corriere(codicefiscalecorriere character) RETURNS SETOF uninadelivery.mezzo_di_trasporto
    LANGUAGE plpgsql
    AS $$
DECLARE
BEGIN
	RETURN QUERY
		SELECT *
		FROM uninadelivery.MEZZO_DI_TRASPORTO AS M
		WHERE uninadelivery.corriere_puo_guidare_mezzo_di_trasporto_stessa_sede(codicefiscalecorriere, M.targa);
END;
$$;


ALTER FUNCTION uninadelivery.get_mezzi_di_trasporto_per_corriere(codicefiscalecorriere character) OWNER TO postgres;

--
-- Name: ordine; Type: TABLE; Schema: uninadelivery; Owner: postgres
--

CREATE TABLE uninadelivery.ordine (
    idordine integer NOT NULL,
    stato uninadelivery.enum_stato DEFAULT 'In elaborazione'::uninadelivery.enum_stato NOT NULL,
    data date NOT NULL,
    orarioinizio time without time zone NOT NULL,
    orariofine time without time zone NOT NULL,
    idspedizione integer,
    emailacquirente character varying,
    dataeffettuazione date NOT NULL,
    idsede integer,
    cap character varying DEFAULT '12345'::character varying NOT NULL,
    "città" character varying DEFAULT 'nomecittà'::character varying NOT NULL,
    via character varying DEFAULT 'via pinco'::character varying NOT NULL,
    civico character varying DEFAULT '1'::character varying NOT NULL,
    edificio character varying,
    CONSTRAINT check_date CHECK (((dataeffettuazione < CURRENT_DATE) AND (data > dataeffettuazione))),
    CONSTRAINT intervallo_per_orari_ordine CHECK (((orarioinizio > '06:00:00'::time without time zone) AND (orariofine < '22:00:00'::time without time zone))),
    CONSTRAINT intervallominimo CHECK ((orariofine >= (orarioinizio + '02:00:00'::interval)))
);


ALTER TABLE uninadelivery.ordine OWNER TO postgres;

--
-- Name: get_ordini(timestamp without time zone, timestamp without time zone, character varying); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.get_ordini(inizio timestamp without time zone, fine timestamp without time zone, emailutente character varying) RETURNS SETOF uninadelivery.ordine
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY (
		SELECT *
		FROM uninadelivery.ORDINE AS O
		WHERE (O.Data + O.OrarioFine > inizio OR inizio IS NULL) AND
			(O.Data + O.OrarioInizio < fine OR fine IS NULL) AND
			(O.EmailAcquirente = emailUtente OR emailUtente IS NULL) AND
			O.Stato <> 'Annullato'
	);
END;
$$;


ALTER FUNCTION uninadelivery.get_ordini(inizio timestamp without time zone, fine timestamp without time zone, emailutente character varying) OWNER TO postgres;

--
-- Name: get_ordini_da_spedire(timestamp without time zone, timestamp without time zone, character varying); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.get_ordini_da_spedire(inizio timestamp without time zone, fine timestamp without time zone, emailutente character varying) RETURNS SETOF uninadelivery.ordine
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY (
		SELECT *
		FROM uninadelivery.ORDINE AS O
		WHERE (O.Data + O.OrarioFine > inizio OR inizio IS NULL) AND
			(O.Data + O.OrarioInizio < fine OR fine IS NULL) AND
			(O.EmailAcquirente = emailUtente OR emailUtente IS NULL) AND
			O.Stato = 'Confermato'
	);
END;
$$;


ALTER FUNCTION uninadelivery.get_ordini_da_spedire(inizio timestamp without time zone, fine timestamp without time zone, emailutente character varying) OWNER TO postgres;

--
-- Name: get_ordini_da_spedire_by_sede(timestamp without time zone, timestamp without time zone, character varying, integer); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.get_ordini_da_spedire_by_sede(inizio timestamp without time zone, fine timestamp without time zone, emailutente character varying, sede integer) RETURNS SETOF uninadelivery.ordine
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY (
        SELECT *
        FROM uninadelivery.ORDINE AS O
        WHERE (O.Data + O.OrarioFine > inizio OR inizio IS NULL) AND
            (O.Data + O.OrarioInizio < fine OR fine IS NULL) AND
            (O.EmailAcquirente = emailUtente OR emailUtente IS NULL) AND
            O.Stato = 'Confermato' AND
            (O.idsede = sede)
    );
END;
$$;


ALTER FUNCTION uninadelivery.get_ordini_da_spedire_by_sede(inizio timestamp without time zone, fine timestamp without time zone, emailutente character varying, sede integer) OWNER TO postgres;

--
-- Name: impossibile_modificare_attributo_ordine(); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.impossibile_modificare_attributo_ordine() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	RAISE 'Si è tentato di modificare un attributo di ORDINE che non è modificabile';
END;
$$;


ALTER FUNCTION uninadelivery.impossibile_modificare_attributo_ordine() OWNER TO postgres;

--
-- Name: impossibile_modificare_dettagli_ordine(); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.impossibile_modificare_dettagli_ordine() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	RAISE 'Si è tentato di modificare un dettagli_ordine dopo la sua creazione';
END;
$$;


ALTER FUNCTION uninadelivery.impossibile_modificare_dettagli_ordine() OWNER TO postgres;

--
-- Name: is_corriere_disponibile(character, timestamp without time zone, timestamp without time zone); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.is_corriere_disponibile(codicefiscaledelcorriere character, inizio timestamp without time zone, fine timestamp without time zone) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF NOT (SELECT C.attivo FROM uninadelivery.CORRIERE AS C WHERE C.codicefiscale = codiceFiscaleDelCorriere) THEN
		RETURN false;
	END IF;
	return NOT EXISTS(
		SELECT *
		FROM uninadelivery.SPEDIZIONE AS S
		WHERE S.codicefiscalecorriere = codiceFiscaleDelCorriere AND (
			S.partenza BETWEEN inizio AND fine OR
			(S.arrivo IS NULL AND S.arrivostimato BETWEEN inizio AND fine) OR
			(S.arrivo BETWEEN inizio AND fine) OR
			(S.partenza <= inizio AND S.arrivostimato >= fine AND S.arrivo IS NULL) OR
			(S.partenza <= inizio AND S.arrivo >= fine AND S.arrivo IS NOT NULL)
		)
	);
END;
$$;


ALTER FUNCTION uninadelivery.is_corriere_disponibile(codicefiscaledelcorriere character, inizio timestamp without time zone, fine timestamp without time zone) OWNER TO postgres;

--
-- Name: is_mezzo_di_trasporto_disponibile(character, timestamp without time zone, timestamp without time zone); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.is_mezzo_di_trasporto_disponibile(targamezzoditrasporto character, inizio timestamp without time zone, fine timestamp without time zone) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	mezzo_attivo BOOLEAN;
BEGIN
	SELECT attivo INTO mezzo_attivo FROM uninadelivery.MEZZO_DI_TRASPORTO AS M WHERE M.targa = targaMezzoDiTrasporto;
	return mezzo_attivo AND NOT EXISTS(
		SELECT *
		FROM uninadelivery.SPEDIZIONE AS S
		WHERE S.targa = targaMezzoDiTrasporto AND (
			S.partenza BETWEEN inizio AND fine OR
			(S.arrivo IS NULL AND S.arrivostimato BETWEEN inizio AND fine) OR
			(S.arrivo BETWEEN inizio AND fine) OR
			(S.partenza <= inizio AND S.arrivostimato >= fine AND S.arrivo IS NULL) OR
			(S.partenza <= inizio AND S.arrivo >= fine AND S.arrivo IS NOT NULL)
		)
	);
END;
$$;


ALTER FUNCTION uninadelivery.is_mezzo_di_trasporto_disponibile(targamezzoditrasporto character, inizio timestamp without time zone, fine timestamp without time zone) OWNER TO postgres;

--
-- Name: non_cancellare_dettagli_ordini(); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.non_cancellare_dettagli_ordini() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	RAISE 'Non è possibile cancellare i dettagli ordine in nessun caso.';
END;
$$;


ALTER FUNCTION uninadelivery.non_cancellare_dettagli_ordini() OWNER TO postgres;

--
-- Name: numero_medio_ordini_in_mese(date); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.numero_medio_ordini_in_mese(data date) RETURNS double precision
    LANGUAGE plpgsql
    AS $$
DECLARE
	primo_giorno_di_mese DATE;
	ultimo_giorno_di_mese DATE;
	tot_ordini FLOAT;
	numero_giorni_in_mese INT;
BEGIN
	primo_giorno_di_mese := date_trunc('month', data)::date;
	ultimo_giorno_di_mese := primo_giorno_di_mese + interval '1 month';
	
	SELECT COUNT(*) INTO tot_ordini
	FROM uninadelivery.ORDINE AS O
	WHERE O.DataEffettuazione >= primo_giorno_di_mese AND O.DataEffettuazione < ultimo_giorno_di_mese AND
		O.stato <> 'Annullato';
	
	numero_giorni_in_mese := ultimo_giorno_di_mese - primo_giorno_di_mese;
	RETURN tot_ordini/numero_giorni_in_mese;
END;
$$;


ALTER FUNCTION uninadelivery.numero_medio_ordini_in_mese(data date) OWNER TO postgres;

--
-- Name: nuovi_dettagli_per_ordine(); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.nuovi_dettagli_per_ordine() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	s uninadelivery.ORDINE.stato%TYPE;
	ids uninadelivery.ORDINE.idsede%TYPE;
BEGIN
	SELECT stato, idsede INTO s, ids FROM uninadelivery.ORDINE AS O WHERE O.idordine = NEW.idordine;
	IF s <> 'In elaborazione' THEN
		RAISE 'Impossibile inserire nuovi dettagli_ordine in riferimento ad un ordine che non sia in elaborazione';
	END IF;
	
	IF (SELECT quantità FROM uninadelivery.disponibilità AS D
		WHERE D.idprodotto = NEW.idprodotto AND D.idsede = ids) < NEW.quantità THEN
		RAISE 'Impossibile creare dettagli_ordine con quantità più grande di quella disponibile nella sede ordine';
	END IF;
	
	UPDATE uninadelivery.disponibilità AS D SET quantità = quantità - NEW.quantità
		WHERE D.idprodotto = NEW.idprodotto AND D.idsede = ids;

	RETURN NEW;
END;
$$;


ALTER FUNCTION uninadelivery.nuovi_dettagli_per_ordine() OWNER TO postgres;

--
-- Name: ordine_con_meno_prodotti_in_mese(date); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.ordine_con_meno_prodotti_in_mese(datainput date) RETURNS uninadelivery.ordine
    LANGUAGE plpgsql
    AS $$
DECLARE
	primo_giorno_di_mese DATE;
	ultimo_giorno_di_mese DATE;
	idordine_massimo uninadelivery.ORDINE.idordine%TYPE;
	ris uninadelivery.ORDINE%ROWTYPE;
BEGIN
	primo_giorno_di_mese := date_trunc('month', dataInput)::date;
	ultimo_giorno_di_mese := primo_giorno_di_mese + interval '1 month';
	SELECT idordine INTO idordine_massimo
	FROM(
		SELECT O.idordine, SUM (D.quantità)
		FROM uninadelivery.ORDINE AS O JOIN uninadelivery.DETTAGLI_ORDINE AS D ON O.IdOrdine = D.IdOrdine
		WHERE O.DataEffettuazione >= primo_giorno_di_mese AND O.DataEffettuazione < ultimo_giorno_di_mese AND O.stato <> 'Annullato'
		GROUP BY O.idordine
		ORDER BY SUM(D.quantità)
		LIMIT 1
	);
	
	SELECT * INTO ris FROM uninadelivery.ORDINE WHERE idordine = idordine_massimo;
	RETURN ris;
END;
$$;


ALTER FUNCTION uninadelivery.ordine_con_meno_prodotti_in_mese(datainput date) OWNER TO postgres;

--
-- Name: ordine_con_piu_prodotti_in_mese(date); Type: FUNCTION; Schema: uninadelivery; Owner: postgres
--

CREATE FUNCTION uninadelivery.ordine_con_piu_prodotti_in_mese(datainput date) RETURNS uninadelivery.ordine
    LANGUAGE plpgsql
    AS $$
DECLARE
	primo_giorno_di_mese DATE;
	ultimo_giorno_di_mese DATE;
	idordine_massimo uninadelivery.ORDINE.idordine%TYPE;
	ris uninadelivery.ORDINE%ROWTYPE;
BEGIN
	primo_giorno_di_mese := date_trunc('month', dataInput)::date;
	ultimo_giorno_di_mese := primo_giorno_di_mese + interval '1 month';
	SELECT idordine INTO idordine_massimo
	FROM(
		SELECT O.idordine, SUM (D.quantità)
		FROM uninadelivery.ORDINE AS O JOIN uninadelivery.DETTAGLI_ORDINE AS D ON O.IdOrdine = D.IdOrdine
		WHERE O.DataEffettuazione >= primo_giorno_di_mese AND O.DataEffettuazione < ultimo_giorno_di_mese AND O.stato <> 'Annullato'
		GROUP BY O.idordine
		ORDER BY SUM(D.quantità) DESC
		LIMIT 1
	);
	
	SELECT * INTO ris FROM uninadelivery.ORDINE WHERE idordine = idordine_massimo;
	RETURN ris;
END;
$$;


ALTER FUNCTION uninadelivery.ordine_con_piu_prodotti_in_mese(datainput date) OWNER TO postgres;

--
-- Name: test(); Type: PROCEDURE; Schema: uninadelivery; Owner: postgres
--

CREATE PROCEDURE uninadelivery.test()
    LANGUAGE plpgsql
    AS $$
DECLARE
	res BOOLEAN;
BEGIN
	CALL uninadelivery.test_autenticazione_operatore();
	CALL uninadelivery.test_corriere_puo_guidare_mezzo_di_trasporto();
	CALL uninadelivery.test_corriere_puo_guidare_mezzo_di_trasporto_stessa_sede();
	CALL uninadelivery.test_is_corriere_disponibile();
	CALL uninadelivery.test_is_mezzo_di_trasporto_disponibile();
END;
$$;


ALTER PROCEDURE uninadelivery.test() OWNER TO postgres;

--
-- Name: test_autenticazione_operatore(); Type: PROCEDURE; Schema: uninadelivery; Owner: postgres
--

CREATE PROCEDURE uninadelivery.test_autenticazione_operatore()
    LANGUAGE plpgsql
    AS $$
DECLARE
	res BOOLEAN;
BEGIN
	RAISE NOTICE 'TEST autenticazione_operatore';
	SELECT * INTO res FROM uninadelivery.autenticazione_operatore('marta.fazzi@unina.delivery.it', 'marta00');
	RAISE NOTICE 'email e password corretti: %', res;
	SELECT * INTO res FROM uninadelivery.autenticazione_operatore('marta.fazzi@unina.delivery.it', 'arta00');
	RAISE NOTICE 'email esistente, password non corretta: %', res;
	SELECT * INTO res FROM uninadelivery.autenticazione_operatore('fazzi@unina.delivery.it', 'marta00');
	RAISE NOTICE 'email inesistente, password esistente: %', res;
	SELECT * INTO res FROM uninadelivery.autenticazione_operatore('carlo.pallino@unina.delivery.it', 'marta00');
	RAISE NOTICE 'email e password esistenti ma non coincidenti: %', res;
END;
$$;


ALTER PROCEDURE uninadelivery.test_autenticazione_operatore() OWNER TO postgres;

--
-- Name: test_corriere_puo_guidare_mezzo_di_trasporto(); Type: PROCEDURE; Schema: uninadelivery; Owner: postgres
--

CREATE PROCEDURE uninadelivery.test_corriere_puo_guidare_mezzo_di_trasporto()
    LANGUAGE plpgsql
    AS $$
DECLARE
	res BOOLEAN;
	corriereB VARCHAR;
	corriereBE VARCHAR;
	corriereB96 VARCHAR;
	corriereC VARCHAR;
	targaB VARCHAR;
	targaBE VARCHAR;
	targaB96 VARCHAR;
	targaC VARCHAR;
BEGIN
	RAISE NOTICE 'TEST corriere_puo_guidare_mezzo_di_trasporto';
	corriereB := 'PLLCRL80A01H703F';
	corriereB96 := 'BRTGNN80A01H703L';
	corriereBE := 'FZZGNN80A01H703G';
	corriereC := 'GLLCRL80A01H703Y';
	targaB := 'FE819PP';
	targaBE := 'DD420BB';
	targaB96 := 'CA991EP';
	targaC := 'CZ711DY';
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereB, targaB);
	RAISE NOTICE 'patente B può guidare mezzo con patente richiesta B: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereB, targaBE);
	RAISE NOTICE 'patente B può guidare mezzo con patente richiesta BE: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereB, targaB96);
	RAISE NOTICE 'patente B può guidare mezzo con patente richiesta B96: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereB, targaC);
	RAISE NOTICE 'patente B può guidare mezzo con patente richiesta C: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereBE, targaB);
	RAISE NOTICE 'patente BE può guidare mezzo con patente richiesta B: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereBE, targaBE);
	RAISE NOTICE 'patente BE può guidare mezzo con patente richiesta BE: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereBE, targaB96);
	RAISE NOTICE 'patente BE può guidare mezzo con patente richiesta B96: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereBE, targaC);
	RAISE NOTICE 'patente BE può guidare mezzo con patente richiesta C: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereB96, targaB);
	RAISE NOTICE 'patente B96 può guidare mezzo con patente richiesta B: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereB96, targaBE);
	RAISE NOTICE 'patente B96 può guidare mezzo con patente richiesta BE: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereB96, targaB96);
	RAISE NOTICE 'patente B96 può guidare mezzo con patente richiesta B96: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereB96, targaC);
	RAISE NOTICE 'patente B96 può guidare mezzo con patente richiesta C: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereC, targaB);
	RAISE NOTICE 'patente C può guidare mezzo con patente richiesta B: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereC, targaBE);
	RAISE NOTICE 'patente C può guidare mezzo con patente richiesta BE: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereC, targaB96);
	RAISE NOTICE 'patente C può guidare mezzo con patente richiesta B96: %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto(corriereC, targaC);
	RAISE NOTICE 'patente C può guidare mezzo con patente richiesta C: %', res;
END;
$$;


ALTER PROCEDURE uninadelivery.test_corriere_puo_guidare_mezzo_di_trasporto() OWNER TO postgres;

--
-- Name: test_corriere_puo_guidare_mezzo_di_trasporto_stessa_sede(); Type: PROCEDURE; Schema: uninadelivery; Owner: postgres
--

CREATE PROCEDURE uninadelivery.test_corriere_puo_guidare_mezzo_di_trasporto_stessa_sede()
    LANGUAGE plpgsql
    AS $$
DECLARE
	res BOOLEAN;
	corriereB VARCHAR;
	corriereBE VARCHAR;
	targaB VARCHAR;
	targaBE VARCHAR;
BEGIN
	corriereB := 'PLLCRL80A01H703F';
	corriereBE := 'FZZGNN80A01H703G';
	targaB := 'FE819PP';
	targaBE := 'DD420BB';
	RAISE NOTICE 'TEST corriere_puo_guidare_mezzo_di_trasporto_stessa_sede';
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto_stessa_sede(corriereB, targaB);
	RAISE NOTICE 'corriere con patente B può guidare mezzo con patente richiesta B nella stessa sede %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto_stessa_sede(corriereBE, targaBE);
	RAISE NOTICE 'corriere con patente BE può guidare mezzo con patente richiesta BE nella stessa sede %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto_stessa_sede(corriereBE, 'FA007WC');
	RAISE NOTICE 'corriere con patente BE può guidare mezzo con patente richiesta BE in sedi diverse %', res;
	SELECT * INTO res FROM uninadelivery.corriere_puo_guidare_mezzo_di_trasporto_stessa_sede(corriereBE, 'FA002WC');
	RAISE NOTICE 'corriere puo guidare veicolo inesistente %', res;
END;
$$;


ALTER PROCEDURE uninadelivery.test_corriere_puo_guidare_mezzo_di_trasporto_stessa_sede() OWNER TO postgres;

--
-- Name: test_is_corriere_disponibile(); Type: PROCEDURE; Schema: uninadelivery; Owner: postgres
--

CREATE PROCEDURE uninadelivery.test_is_corriere_disponibile()
    LANGUAGE plpgsql
    AS $$
DECLARE
	res BOOLEAN;
BEGIN
	RAISE NOTICE 'TEST is_corriere_disponibile';
	SELECT * INTO res FROM uninadelivery.is_corriere_disponibile ('GRSLCA80A01H703O', '2024-1-15 08:00', '2024-1-15 08:00');
	RAISE NOTICE 'corriere che non ha spedizioni è disponibile %', res;
	SELECT * INTO res FROM uninadelivery.is_corriere_disponibile ('BNCLRA80A01H703E', '2024-1-15 08:00', '2024-1-15 09:00');
	RAISE NOTICE 'corriere è disponibile prima della spedizione %', res;
	SELECT * INTO res FROM uninadelivery.is_corriere_disponibile ('BNCLRA80A01H703E', '2024-1-15 08:00', '2024-1-15 10:01');
	RAISE NOTICE 'corriere è disponibile (inizio libero, fine durante spedizione) %', res;
	SELECT * INTO res FROM uninadelivery.is_corriere_disponibile ('BNCLRA80A01H703E', '2024-1-15 11:30', '2024-1-15 13:00');
	RAISE NOTICE 'corriere è disponibile (inizio durante spedizione, fine libero) %', res;
	SELECT * INTO res FROM uninadelivery.is_corriere_disponibile ('BNCLRA80A01H703E', '2024-1-15 11:30', '2024-1-15 12:00');
	RAISE NOTICE 'corriere è disponibile durante spedizione %', res;
	SELECT * INTO res FROM uninadelivery.is_corriere_disponibile ('BNCLRA80A01H703E', '2024-1-15 13:00', '2024-1-15 14:00');
	RAISE NOTICE 'corriere è disponibile in mezzo a due spedizioni %', res;
	SELECT * INTO res FROM uninadelivery.is_corriere_disponibile ('BNCLRA80A01H703E', '2024-1-15 07:00', '2024-1-15 13:00');
	RAISE NOTICE 'corriere è disponibile in un intervallo che ingloba una spedizione %', res;
	SELECT * INTO res FROM uninadelivery.is_corriere_disponibile ('BNCLRA80A01H703E', '2024-1-15 17:00', '2024-1-15 18:00');
	RAISE NOTICE 'corriere è disponibile in un intervallo tra arrivo effettivo e quello stimato %', res;
	SELECT * INTO res FROM uninadelivery.is_corriere_disponibile ('BNCLRA80A01H703E', '2024-1-16 17:00', '2024-1-16 18:00');
	RAISE NOTICE 'corriere è disponibile in un intervallo tra arrivo effettivo e quello stimato (ritardo) %', res;
END;
$$;


ALTER PROCEDURE uninadelivery.test_is_corriere_disponibile() OWNER TO postgres;

--
-- Name: test_is_mezzo_di_trasporto_disponibile(); Type: PROCEDURE; Schema: uninadelivery; Owner: postgres
--

CREATE PROCEDURE uninadelivery.test_is_mezzo_di_trasporto_disponibile()
    LANGUAGE plpgsql
    AS $$
DECLARE
	res BOOLEAN;
BEGIN
	RAISE NOTICE 'TEST is_corriere_disponibile';
	SELECT * INTO res FROM uninadelivery.is_mezzo_di_trasporto_disponibile ('EH273DC', '2024-1-15 08:00', '2024-1-15 08:00');
	RAISE NOTICE 'mezzo che non ha spedizioni è disponibile %', res;
	SELECT * INTO res FROM uninadelivery.is_mezzo_di_trasporto_disponibile ('EJ734LV', '2024-1-15 08:00', '2024-1-15 09:00');
	RAISE NOTICE 'mezzo è disponibile prima della spedizione %', res;
	SELECT * INTO res FROM uninadelivery.is_mezzo_di_trasporto_disponibile ('EJ734LV', '2024-1-15 08:00', '2024-1-15 10:01');
	RAISE NOTICE 'mezzo è disponibile (inizio libero, fine durante spedizione) %', res;
	SELECT * INTO res FROM uninadelivery.is_mezzo_di_trasporto_disponibile ('EJ734LV', '2024-1-15 11:30', '2024-1-15 13:00');
	RAISE NOTICE 'mezzo è disponibile (inizio durante spedizione, fine libero) %', res;
	SELECT * INTO res FROM uninadelivery.is_mezzo_di_trasporto_disponibile ('EJ734LV', '2024-1-15 11:30', '2024-1-15 12:00');
	RAISE NOTICE 'mezzo è disponibile durante spedizione %', res;
	SELECT * INTO res FROM uninadelivery.is_mezzo_di_trasporto_disponibile ('EJ734LV', '2024-1-15 13:00', '2024-1-15 14:00');
	RAISE NOTICE 'mezzo è disponibile in mezzo a due spedizioni %', res;
	SELECT * INTO res FROM uninadelivery.is_mezzo_di_trasporto_disponibile ('EJ734LV', '2024-1-15 07:00', '2024-1-15 13:00');
	RAISE NOTICE 'mezzo è disponibile in un intervallo che ingloba una spedizione %', res;
	SELECT * INTO res FROM uninadelivery.is_mezzo_di_trasporto_disponibile ('EJ734LV', '2024-1-15 17:00', '2024-1-15 18:00');
	RAISE NOTICE 'mezzo è disponibile in un intervallo tra arrivo effettivo e quello stimato %', res;
	SELECT * INTO res FROM uninadelivery.is_mezzo_di_trasporto_disponibile ('EJ734LV', '2024-1-16 17:00', '2024-1-16 18:00');
	RAISE NOTICE 'mezzo è disponibile in un intervallo tra arrivo effettivo e quello stimato (ritardo) %', res;
END;
$$;


ALTER PROCEDURE uninadelivery.test_is_mezzo_di_trasporto_disponibile() OWNER TO postgres;

--
-- Name: acquirente; Type: TABLE; Schema: uninadelivery; Owner: postgres
--

CREATE TABLE uninadelivery.acquirente (
    email character varying NOT NULL,
    nome character varying NOT NULL,
    cognome character varying NOT NULL,
    password character varying NOT NULL,
    numerotelefono character(10),
    CONSTRAINT acquirente_email_check CHECK (((email)::text ~~ '%@%.%'::text)),
    CONSTRAINT telefono_valido CHECK ((numerotelefono ~ similar_to_escape('[0-9]{10}'::text)))
);


ALTER TABLE uninadelivery.acquirente OWNER TO postgres;

--
-- Name: corriere_idsede_seq; Type: SEQUENCE; Schema: uninadelivery; Owner: postgres
--

CREATE SEQUENCE uninadelivery.corriere_idsede_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE uninadelivery.corriere_idsede_seq OWNER TO postgres;

--
-- Name: corriere_idsede_seq; Type: SEQUENCE OWNED BY; Schema: uninadelivery; Owner: postgres
--

ALTER SEQUENCE uninadelivery.corriere_idsede_seq OWNED BY uninadelivery.corriere.idsede;


--
-- Name: dettagli_ordine; Type: TABLE; Schema: uninadelivery; Owner: postgres
--

CREATE TABLE uninadelivery.dettagli_ordine (
    idordine integer NOT NULL,
    "quantità" integer DEFAULT 1 NOT NULL,
    idprodotto integer NOT NULL,
    CONSTRAINT "dettagli_ordine_quantità_check" CHECK (("quantità" > 0))
);


ALTER TABLE uninadelivery.dettagli_ordine OWNER TO postgres;

--
-- Name: dettagli_ordine_idordine_seq; Type: SEQUENCE; Schema: uninadelivery; Owner: postgres
--

CREATE SEQUENCE uninadelivery.dettagli_ordine_idordine_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE uninadelivery.dettagli_ordine_idordine_seq OWNER TO postgres;

--
-- Name: dettagli_ordine_idordine_seq; Type: SEQUENCE OWNED BY; Schema: uninadelivery; Owner: postgres
--

ALTER SEQUENCE uninadelivery.dettagli_ordine_idordine_seq OWNED BY uninadelivery.dettagli_ordine.idordine;


--
-- Name: disponibilità; Type: TABLE; Schema: uninadelivery; Owner: postgres
--

CREATE TABLE uninadelivery."disponibilità" (
    "quantità" integer NOT NULL,
    idprodotto integer NOT NULL,
    idsede integer NOT NULL,
    CONSTRAINT "disponibilità_quantità_check" CHECK (("quantità" >= 0))
);


ALTER TABLE uninadelivery."disponibilità" OWNER TO postgres;

--
-- Name: metodo_pagamento; Type: TABLE; Schema: uninadelivery; Owner: postgres
--

CREATE TABLE uninadelivery.metodo_pagamento (
    numerocarta character(16) NOT NULL,
    tipo character varying NOT NULL,
    email character varying NOT NULL,
    CONSTRAINT numero_carta_valido CHECK ((numerocarta ~ similar_to_escape('[0-9]{16}'::text)))
);


ALTER TABLE uninadelivery.metodo_pagamento OWNER TO postgres;

--
-- Name: mezzo_di_trasporto_idsede_seq; Type: SEQUENCE; Schema: uninadelivery; Owner: postgres
--

CREATE SEQUENCE uninadelivery.mezzo_di_trasporto_idsede_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE uninadelivery.mezzo_di_trasporto_idsede_seq OWNER TO postgres;

--
-- Name: mezzo_di_trasporto_idsede_seq; Type: SEQUENCE OWNED BY; Schema: uninadelivery; Owner: postgres
--

ALTER SEQUENCE uninadelivery.mezzo_di_trasporto_idsede_seq OWNED BY uninadelivery.mezzo_di_trasporto.idsede;


--
-- Name: operatore; Type: TABLE; Schema: uninadelivery; Owner: postgres
--

CREATE TABLE uninadelivery.operatore (
    codicefiscale character(16) NOT NULL,
    nome character varying NOT NULL,
    cognome character varying NOT NULL,
    salario numeric NOT NULL,
    email character varying NOT NULL,
    password character varying NOT NULL,
    idsede integer NOT NULL,
    attivo boolean DEFAULT true NOT NULL,
    CONSTRAINT codice_fiscale_valido CHECK ((codicefiscale ~ similar_to_escape('[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]'::text))),
    CONSTRAINT email_aziendale CHECK (((email)::text ~~ '%@unina.delivery.it'::text)),
    CONSTRAINT salario_positivo CHECK ((salario > (0)::numeric))
);


ALTER TABLE uninadelivery.operatore OWNER TO postgres;

--
-- Name: operatore_idsede_seq; Type: SEQUENCE; Schema: uninadelivery; Owner: postgres
--

CREATE SEQUENCE uninadelivery.operatore_idsede_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE uninadelivery.operatore_idsede_seq OWNER TO postgres;

--
-- Name: operatore_idsede_seq; Type: SEQUENCE OWNED BY; Schema: uninadelivery; Owner: postgres
--

ALTER SEQUENCE uninadelivery.operatore_idsede_seq OWNED BY uninadelivery.operatore.idsede;


--
-- Name: ordine_idordine_seq; Type: SEQUENCE; Schema: uninadelivery; Owner: postgres
--

CREATE SEQUENCE uninadelivery.ordine_idordine_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE uninadelivery.ordine_idordine_seq OWNER TO postgres;

--
-- Name: ordine_idordine_seq; Type: SEQUENCE OWNED BY; Schema: uninadelivery; Owner: postgres
--

ALTER SEQUENCE uninadelivery.ordine_idordine_seq OWNED BY uninadelivery.ordine.idordine;


--
-- Name: ordine_idspedizione_seq; Type: SEQUENCE; Schema: uninadelivery; Owner: postgres
--

CREATE SEQUENCE uninadelivery.ordine_idspedizione_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE uninadelivery.ordine_idspedizione_seq OWNER TO postgres;

--
-- Name: ordine_idspedizione_seq; Type: SEQUENCE OWNED BY; Schema: uninadelivery; Owner: postgres
--

ALTER SEQUENCE uninadelivery.ordine_idspedizione_seq OWNED BY uninadelivery.ordine.idspedizione;


--
-- Name: prodotto; Type: TABLE; Schema: uninadelivery; Owner: postgres
--

CREATE TABLE uninadelivery.prodotto (
    idprodotto integer NOT NULL,
    nome character varying NOT NULL,
    descrizione character varying,
    peso double precision NOT NULL,
    prezzo money NOT NULL,
    CONSTRAINT prodotto_prezzo_check CHECK ((prezzo > money(0)))
);


ALTER TABLE uninadelivery.prodotto OWNER TO postgres;

--
-- Name: prodotto_idprodotto_seq; Type: SEQUENCE; Schema: uninadelivery; Owner: postgres
--

CREATE SEQUENCE uninadelivery.prodotto_idprodotto_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE uninadelivery.prodotto_idprodotto_seq OWNER TO postgres;

--
-- Name: prodotto_idprodotto_seq; Type: SEQUENCE OWNED BY; Schema: uninadelivery; Owner: postgres
--

ALTER SEQUENCE uninadelivery.prodotto_idprodotto_seq OWNED BY uninadelivery.prodotto.idprodotto;


--
-- Name: sede; Type: TABLE; Schema: uninadelivery; Owner: postgres
--

CREATE TABLE uninadelivery.sede (
    idsede integer NOT NULL,
    nome character varying NOT NULL,
    "città" character varying NOT NULL,
    cap character varying NOT NULL,
    via character varying NOT NULL,
    civico character varying NOT NULL,
    edificio character varying
);


ALTER TABLE uninadelivery.sede OWNER TO postgres;

--
-- Name: sede_id_sede_seq; Type: SEQUENCE; Schema: uninadelivery; Owner: postgres
--

CREATE SEQUENCE uninadelivery.sede_id_sede_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE uninadelivery.sede_id_sede_seq OWNER TO postgres;

--
-- Name: sede_id_sede_seq; Type: SEQUENCE OWNED BY; Schema: uninadelivery; Owner: postgres
--

ALTER SEQUENCE uninadelivery.sede_id_sede_seq OWNED BY uninadelivery.sede.idsede;


--
-- Name: spedizione; Type: TABLE; Schema: uninadelivery; Owner: postgres
--

CREATE TABLE uninadelivery.spedizione (
    idspedizione integer NOT NULL,
    partenza timestamp without time zone NOT NULL,
    arrivo timestamp without time zone,
    arrivostimato timestamp without time zone NOT NULL,
    codicefiscalecorriere character(16) NOT NULL,
    codicefiscaleoperatore character(16) NOT NULL,
    targa character(7) NOT NULL,
    CONSTRAINT controlloarrivostimatoregolare CHECK (((arrivostimato >= partenza) AND (arrivostimato <= (partenza + '09:00:00'::interval))))
);


ALTER TABLE uninadelivery.spedizione OWNER TO postgres;

--
-- Name: spedizione_idspedizione_seq; Type: SEQUENCE; Schema: uninadelivery; Owner: postgres
--

CREATE SEQUENCE uninadelivery.spedizione_idspedizione_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE uninadelivery.spedizione_idspedizione_seq OWNER TO postgres;

--
-- Name: spedizione_idspedizione_seq; Type: SEQUENCE OWNED BY; Schema: uninadelivery; Owner: postgres
--

ALTER SEQUENCE uninadelivery.spedizione_idspedizione_seq OWNED BY uninadelivery.spedizione.idspedizione;


--
-- Name: corriere idsede; Type: DEFAULT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.corriere ALTER COLUMN idsede SET DEFAULT nextval('uninadelivery.corriere_idsede_seq'::regclass);


--
-- Name: mezzo_di_trasporto idsede; Type: DEFAULT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.mezzo_di_trasporto ALTER COLUMN idsede SET DEFAULT nextval('uninadelivery.mezzo_di_trasporto_idsede_seq'::regclass);


--
-- Name: operatore idsede; Type: DEFAULT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.operatore ALTER COLUMN idsede SET DEFAULT nextval('uninadelivery.operatore_idsede_seq'::regclass);


--
-- Name: ordine idordine; Type: DEFAULT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.ordine ALTER COLUMN idordine SET DEFAULT nextval('uninadelivery.ordine_idordine_seq'::regclass);


--
-- Name: prodotto idprodotto; Type: DEFAULT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.prodotto ALTER COLUMN idprodotto SET DEFAULT nextval('uninadelivery.prodotto_idprodotto_seq'::regclass);


--
-- Name: sede idsede; Type: DEFAULT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.sede ALTER COLUMN idsede SET DEFAULT nextval('uninadelivery.sede_id_sede_seq'::regclass);


--
-- Name: spedizione idspedizione; Type: DEFAULT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.spedizione ALTER COLUMN idspedizione SET DEFAULT nextval('uninadelivery.spedizione_idspedizione_seq'::regclass);


--
-- Data for Name: acquirente; Type: TABLE DATA; Schema: uninadelivery; Owner: postgres
--

INSERT INTO uninadelivery.acquirente VALUES
	('elpibedeoro@libero.it', 'Diego Armando', 'Maradona', 'ForzaNapoli10', NULL),
	('lucab@yahoo.com', 'Luca', 'Barrella', 'pizza$gourmet', NULL),
	('lucarosso@gmail.com', 'Luca', 'Rosso', 'RedNBlack', '3337891234'),
	('sangiovanni.mara@yahoo.com', 'Mara', 'Sangiovanni', '""; DROP TABLE UTENTI;', NULL),
	('marid@gmail.com', 'Mari', 'Della Valle', 'banana', NULL),
	('fab@gmail.com', 'Fabrizio', 'Apuzzo', 'i<3mari', '3345679911'),
	('tur1ng@libero.it', 'Alan', 'Turing', 'succo%ACE', NULL),
	('itsamemario@gmail.it', 'Mario', 'Mario', 'WonderSeed', NULL),
	('itsluigi@yahoo.com', 'Luigi', 'Mario', 'funghi1up', NULL),
	('lovelace@yahoo.com', 'Ada', 'Lovelace', '01010110', NULL),
	('nesli@gmail.com', 'Francesco', 'Tarducci', 'La fine', NULL),
	('bjork@gmail.com', 'Bjork', 'Guomundsdottir', 'ArmyOfMe', NULL),
	('franclascign@gmail.com', 'Francesco', 'Inserra', 'Maiden22', NULL),
	('mariopal@gmail.com', 'Mario', 'Inserra', 'password', NULL),
	('finanziere@libero.it', 'Sebastiano', 'Inserra', 'Sebastiano102', NULL),
	('markzuck@outlook.com', 'Mark', 'Zuckerberg', 'FacciaDiLibro', NULL),
	('javasucks@gmail.com', 'Guido', 'van Rossum', 'import-antigravity', NULL),
	('paulallen@asia.com', 'Paul', 'Allen', 'Allen23344556', NULL),
	('rickdeckard@gmail.com', 'Rick', 'Deckard', 'Blade2049', '0495858123'),
	('billy@outlook.com', 'Bill', 'Gates', 'SD4-57.E2$', NULL);


--
-- Data for Name: corriere; Type: TABLE DATA; Schema: uninadelivery; Owner: postgres
--

INSERT INTO uninadelivery.corriere VALUES
	('FRRGNN80A01H703V', 'Giovanni', 'Ferrari', 1400.00, 'C', 1, true),
	('CNTMRC80A01H703W', 'Marco', 'Conti', 1300.00, 'C', 2, true),
	('GLLCRL80A01H703Y', 'Carla', 'Galli', 1200.00, 'C', 4, true),
	('RCCFRN80A01H703Z', 'Francesco', 'Ricci', 1000.00, 'BE', 1, true),
	('MNTLRA80A01H703B', 'Laura', 'Monti', 1200.00, 'C', 3, true),
	('RSSLCA80A01H703C', 'Luca', 'Rossi', 1100.00, 'C', 4, true),
	('VRDNTN80A01H703D', 'Antonio', 'Verdi', 1000.00, 'BE', 1, true),
	('BNCLRA80A01H703E', 'Laura', 'Bianchi', 1000.00, 'C', 2, true),
	('PLLCRL80A01H703F', 'Carlo', 'Pallino', 1100.00, 'B', 3, true),
	('FZZGNN80A01H703G', 'Gianna', 'Fazzi', 1300.00, 'BE', 4, true),
	('CSTLNA80A01H703H', 'Elena', 'Costa', 1200.00, 'C', 1, true),
	('MRLFRN80A01H703I', 'Francesco', 'Morelli', 1300.00, 'C', 2, true),
	('LNGMRC80A01H703M', 'Marco', 'Longhi', 1300.00, 'C', 4, true),
	('CLLNTN80A01H703N', 'Antonella', 'Colli', 1200.00, 'C', 1, true),
	('GRSLCA80A01H703O', 'Luca', 'Grassi', 1100.00, 'BE', 2, true),
	('MZZLRA80A01H703P', 'Laura', 'Mazzi', 1050.00, 'C', 3, true),
	('RMBRRT80A01H703Q', 'Roberta', 'Romano', 1000.00, 'C', 4, true),
	('MRTMRA80A01H703X', 'Maria', 'Martini', 1500.00, 'B96', 3, true),
	('SNTLCA80A01H703A', 'Luca', 'Santi', 1400.00, 'B96', 2, true),
	('BRTGNN80A01H703L', 'Gianni', 'Baratti', 1200.00, 'B96', 3, true);


--
-- Data for Name: dettagli_ordine; Type: TABLE DATA; Schema: uninadelivery; Owner: postgres
--

INSERT INTO uninadelivery.dettagli_ordine VALUES
	(29, 1, 25),
	(29, 1, 2),
	(30, 1, 10),
	(30, 1, 17),
	(30, 1, 1),
	(31, 1, 20),
	(31, 2, 23),
	(32, 1, 25),
	(33, 4, 14),
	(34, 2, 9),
	(34, 4, 21),
	(35, 1, 19),
	(35, 1, 17),
	(35, 1, 25),
	(35, 2, 3),
	(36, 1, 11),
	(36, 1, 18),
	(37, 1, 2),
	(37, 1, 21),
	(38, 2, 22),
	(39, 1, 24),
	(41, 2, 8),
	(41, 4, 7),
	(28, 3, 19),
	(28, 3, 17),
	(47, 2, 2),
	(46, 10000, 1),
	(48, 10000, 1);


--
-- Data for Name: disponibilità; Type: TABLE DATA; Schema: uninadelivery; Owner: postgres
--

INSERT INTO uninadelivery."disponibilità" VALUES
	(13, 2, 1),
	(1, 1, 1),
	(2, 1, 2),
	(5, 1, 4),
	(8, 2, 2),
	(7, 2, 3),
	(5, 2, 4),
	(6, 3, 1),
	(6, 3, 2),
	(3, 3, 4),
	(2, 4, 2),
	(3, 4, 3),
	(3, 5, 1),
	(2, 5, 3),
	(3, 5, 4),
	(5, 6, 1),
	(5, 6, 2),
	(3, 6, 3),
	(4, 6, 4),
	(10, 7, 1),
	(8, 7, 2),
	(6, 7, 3),
	(9, 7, 4),
	(8, 8, 1),
	(4, 8, 2),
	(6, 9, 1),
	(7, 9, 2),
	(6, 9, 3),
	(6, 9, 4),
	(2, 10, 1),
	(3, 10, 2),
	(3, 10, 3),
	(4, 10, 4),
	(2, 11, 1),
	(4, 11, 2),
	(4, 11, 3),
	(5, 11, 4),
	(4, 12, 1),
	(3, 12, 2),
	(3, 12, 3),
	(3, 12, 4),
	(6, 13, 1),
	(7, 13, 2),
	(4, 13, 3),
	(5, 13, 4),
	(16, 14, 1),
	(12, 14, 2),
	(14, 14, 3),
	(26, 14, 4),
	(6, 15, 1),
	(7, 15, 2),
	(8, 15, 3),
	(5, 15, 4),
	(6, 16, 1),
	(3, 16, 2),
	(5, 16, 4),
	(16, 17, 1),
	(22, 17, 2),
	(28, 17, 3),
	(15, 17, 4),
	(26, 18, 1),
	(32, 18, 2),
	(28, 18, 3),
	(30, 18, 4),
	(8, 19, 3),
	(4, 19, 4),
	(16, 20, 1),
	(22, 20, 2),
	(28, 20, 3),
	(15, 20, 4),
	(50, 21, 1),
	(42, 21, 2),
	(40, 21, 3),
	(38, 21, 4),
	(10, 22, 1),
	(8, 22, 2),
	(16, 22, 3),
	(13, 22, 4),
	(36, 23, 1),
	(32, 23, 2),
	(38, 23, 3),
	(35, 23, 4),
	(7, 24, 1),
	(12, 24, 2),
	(5, 24, 3),
	(8, 24, 4),
	(3, 25, 1),
	(2, 25, 3),
	(2, 25, 4);


--
-- Data for Name: metodo_pagamento; Type: TABLE DATA; Schema: uninadelivery; Owner: postgres
--



--
-- Data for Name: mezzo_di_trasporto; Type: TABLE DATA; Schema: uninadelivery; Owner: postgres
--

INSERT INTO uninadelivery.mezzo_di_trasporto VALUES
	('DC388FR', 'Autocarro Pesante', 'C', 4000, 1, true),
	('ES231AW', 'Autocarro Pesante', 'C', 5000, 1, true),
	('DK992LK', 'Autocarro Pesante', 'C', 5000, 1, true),
	('FA518DM', 'Autocarro Pesante', 'C', 4000, 2, true),
	('EJ734LV', 'Autocarro Pesante', 'C', 4000, 2, true),
	('GB823US', 'Autocarro Pesante', 'C', 4600, 3, true),
	('DN723CC', 'Autocarro Pesante', 'C', 4600, 3, true),
	('CZ711DY', 'Autocarro Pesante', 'C', 4000, 4, true),
	('DE102TF', 'Autocarro Pesante', 'C', 5000, 4, true),
	('EH273DC', 'Autocarro Leggero', 'BE', 2000, 1, true),
	('DL442FG', 'Autocarro Leggero', 'BE', 3500, 1, true),
	('FA007WC', 'Autocarro Leggero', 'BE', 3500, 2, true),
	('DF643DA', 'Autocarro Leggero', 'BE', 3000, 2, true),
	('DE069FK', 'Autocarro Leggero', 'BE', 3500, 2, true),
	('DD420BB', 'Autocarro Leggero', 'BE', 3000, 3, true),
	('GH588RY', 'Autocarro Leggero', 'BE', 3000, 3, true),
	('DP180SH', 'Autocarro Leggero', 'BE', 3000, 4, true),
	('CA991EP', 'Autocarro Leggero con Rimorchio', 'B96', 4250, 3, true),
	('FE819PP', 'Autoveicolo con Rimorchio', 'B', 750, 3, true);


--
-- Data for Name: operatore; Type: TABLE DATA; Schema: uninadelivery; Owner: postgres
--

INSERT INTO uninadelivery.operatore VALUES
	('PLLCRL88P08M834L', 'Carlo', 'Pallino', 2200.00, 'carlo.pallino@unina.delivery.it', 'carlo123', 1, true),
	('FRRMRC03D12J124M', 'Marco', 'Lamborghini', 2100.00, 'marco.lambo@unina.delivery.it', 'marco123', 1, true),
	('CNTLRA76B42K219N', 'Laura', 'Conti', 1700.00, 'laura.conti@unina.delivery.it', 'laura456', 2, true),
	('MRTRRT81A25H142B', 'Roberto', 'Langellotto', 2100.00, 'roberto.lang@unina.delivery.it', 'robertoL789', 3, true),
	('RCCMCH90E13K053D', 'Michele', 'Ricci', 1900.00, 'michele.ricci@unina.delivery.it', 'michele345', 1, true),
	('SNTGNN76T59I264E', 'Gianna', 'Santi', 2000.00, 'gianna.santi@unina.delivery.it', 'gianna678', 2, true),
	('MNTLCA82H04S394F', 'Luca', 'Monti', 1700.00, 'luca.monti@unina.delivery.it', 'luca901', 3, true),
	('RSSLRA64L46B347G', 'Laura', 'Rosso', 1800.00, 'laura.rox@unina.delivery.it', 'laura234', 4, true),
	('VRDGRG88M28L351H', 'Giorgio', 'Verdi', 1900.00, 'giorgio.verdi@unina.delivery.it', 'g10rgio567', 1, true),
	('RSSCLD88P08M834L', 'Claudio', 'Russo', 2200.00, 'claudio.russo@unina.delivery.it', 'claudio123', 3, true),
	('PRASMN88P08M834L', 'Simone', 'Pera', 2000.00, 'sim.pera@unina.delivery.it', 'ricotta3pera', 1, true),
	('FZZMRA92P41O949M', 'Marta', 'Fazzi', 1600.00, 'marta.fazzi@unina.delivery.it', 'marta00', 4, true),
	('BNCLCA75R11A294I', 'Luca', 'Bianchi', 2100.00, 'luca.bianchi@unina.delivery.it', 'luca890', 2, true),
	('AAABBB00C11D123E', 'Nome', 'Cognome', 1500, 'sede1@unina.delivery.it', '123', 1, true);


--
-- Data for Name: ordine; Type: TABLE DATA; Schema: uninadelivery; Owner: postgres
--

INSERT INTO uninadelivery.ordine VALUES
	(33, 'Confermato', '2024-01-15', '18:00:00', '20:00:00', NULL, 'sangiovanni.mara@yahoo.com', '2023-12-31', 1, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(32, 'Confermato', '2024-01-15', '10:00:00', '13:00:00', NULL, 'lucarosso@gmail.com', '2024-01-01', 3, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(28, 'Confermato', '2024-01-15', '08:00:00', '12:00:00', 1, 'lucarosso@gmail.com', '2024-01-01', 1, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(45, 'Annullato', '2024-01-15', '15:00:00', '17:00:00', NULL, 'billy@outlook.com', '2024-01-05', 1, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(47, 'Annullato', '2024-01-15', '15:00:00', '17:00:00', NULL, NULL, '2024-01-05', 1, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(46, 'Confermato', '2024-01-15', '15:00:00', '17:00:00', NULL, 'billy@outlook.com', '2024-01-05', 1, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(48, 'Confermato', '2024-01-16', '08:00:00', '15:00:00', NULL, 'fab@gmail.com', '2024-01-05', 1, '2839', '', '', '', NULL),
	(41, 'Confermato', '2024-01-15', '09:00:00', '11:30:00', NULL, 'nesli@gmail.com', '2023-12-08', 1, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(29, 'Confermato', '2024-01-15', '09:00:00', '13:00:00', NULL, 'sangiovanni.mara@yahoo.com', '2024-01-01', 3, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(30, 'Confermato', '2024-01-15', '08:00:00', '10:00:00', NULL, 'elpibedeoro@libero.it', '2024-01-01', 3, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(31, 'Confermato', '2024-01-15', '10:00:00', '12:30:00', NULL, 'sangiovanni.mara@yahoo.com', '2024-01-01', 4, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(34, 'Confermato', '2024-01-15', '13:00:00', '16:30:00', NULL, 'lucab@yahoo.com', '2023-12-31', 3, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(35, 'Confermato', '2024-01-15', '08:00:00', '12:00:00', NULL, 'itsamemario@gmail.it', '2023-12-31', 2, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(36, 'Confermato', '2024-01-15', '08:00:00', '16:00:00', NULL, 'tur1ng@libero.it', '2023-12-28', 4, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(37, 'Confermato', '2024-01-15', '08:00:00', '10:00:00', NULL, 'lovelace@yahoo.com', '2023-12-30', 2, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(38, 'Confermato', '2024-01-15', '17:00:00', '19:30:00', NULL, 'tur1ng@libero.it', '2023-12-01', 3, '12345', 'nomecittà', 'via pinco', '1', NULL),
	(39, 'Confermato', '2024-01-15', '10:00:00', '12:00:00', NULL, 'bjork@gmail.com', '2023-12-01', 3, '12345', 'nomecittà', 'via pinco', '1', NULL);


--
-- Data for Name: prodotto; Type: TABLE DATA; Schema: uninadelivery; Owner: postgres
--

INSERT INTO uninadelivery.prodotto VALUES
	(1, 'Divano in pelle', 'Divano a tre posti in pelle marrone con cuscini morbidi', 55, '€ 899,00'),
	(2, 'Tavolino da caffè', 'Tavolino da caffè in legno massello con ripiano inferiore', 12, '€ 199,00'),
	(3, 'Poltrona reclinabile', 'Poltrona reclinabile in tessuto grigio con poggiapiedi estraibile', 25, '€ 399,00'),
	(4, 'Libreria', 'Libreria in metallo e vetro con cinque ripiani regolabili', 18, '€ 249,00'),
	(5, 'Scrivania', 'Scrivania in legno bianco con tre cassetti e piano inclinabile', 20, '€ 299,00'),
	(6, 'Sedia da ufficio', 'Sedia da ufficio ergonomica con ruote e braccioli regolabili', 15, '€ 149,00'),
	(7, 'Lampada da terra', 'Lampada da terra in stile moderno con paralume nero e base cromata', 5, '€ 79,00'),
	(8, 'Specchio', 'Specchio ovale con cornice dorata e gancio per appendere', 3, '€ 49,00'),
	(9, 'Comodino', 'Comodino in legno scuro con un cassetto e una mensola', 8, '€ 99,00'),
	(10, 'Letto matrimoniale', 'Letto matrimoniale in legno chiaro con testiera imbottita e doghe', 40, '€ 599,00'),
	(11, 'Cassettiera', 'Cassettiera in legno chiaro con sei cassetti e maniglie in metallo', 30, '€ 399,00'),
	(12, 'Armadio', 'Armadio in legno chiaro con due ante scorrevoli e uno specchio interno', 50, '€ 699,00'),
	(13, 'Tappeto', 'Tappeto in lana beige con motivi geometrici colorati', 10, '€ 129,00'),
	(14, 'Cuscino', 'Cuscino in cotone bianco con stampa floreale e zip', 1, '€ 19,00'),
	(15, 'Coperta', 'Coperta in pile blu con bordo in pelliccia sintetica', 3, '€ 39,00'),
	(16, 'Orologio da parete', 'Orologio da parete in plastica nera con numeri grandi e lancette luminose', 2, '€ 29,00'),
	(17, 'Vaso', 'Vaso in ceramica verde con decorazioni in rilievo e forma a bottiglia', 1, '€ 15,00'),
	(18, 'Pianta', 'Pianta artificiale in vaso con foglie verdi e fiori rossi', 2, '€ 25,00'),
	(19, 'Quadro', 'Quadro con stampa di una città notturna e cornice nera', 4, '€ 59,00'),
	(20, 'Portacandele', 'Portacandele in metallo argentato con forma di stella e vetro colorato', 1, '€ 9,00'),
	(21, 'Candela', 'Candela profumata con aroma di vaniglia e contenitore in vetro', 1, '€ 12,00'),
	(22, 'Portaombrelli', 'Portaombrelli in metallo bianco con fori e base a goccia', 3, '€ 19,00'),
	(23, 'Ombrello', 'Ombrello pieghevole con telaio in acciaio e tessuto impermeabile a righe', 1, '€ 12,00'),
	(24, 'Appendiabiti', 'Appendiabiti da parete in legno con cinque ganci in metallo', 2, '€ 25,00'),
	(25, 'Divano in pelle nera', 'Divano a tre posti in pelle nera con cuscini morbidi decorati', 55, '€ 999,00');


--
-- Data for Name: sede; Type: TABLE DATA; Schema: uninadelivery; Owner: postgres
--

INSERT INTO uninadelivery.sede VALUES
	(1, 'UninaDelivery NAPOLI', 'Napoli', '80133', 'Corso Umberto I', '102', NULL),
	(2, 'UninaDelivery MARADONA', 'Napoli', '80125', 'Viale Traiano', '374', '15'),
	(3, 'UninaDelivery Centro Campania', 'Marcianise', '81025', 'Aurno', '87', 'B2'),
	(4, 'UninaDelivery SORRENTO', 'Sorrento', '80063', 'Via degli Aranci', '17', 'A');


--
-- Data for Name: spedizione; Type: TABLE DATA; Schema: uninadelivery; Owner: postgres
--

INSERT INTO uninadelivery.spedizione VALUES
	(1, '2024-01-15 10:00:00', NULL, '2024-01-15 12:30:00', 'BNCLRA80A01H703E', 'BNCLCA75R11A294I', 'EJ734LV'),
	(2, '2024-01-15 15:00:00', '2024-01-15 16:30:00', '2024-01-15 19:30:00', 'BNCLRA80A01H703E', 'BNCLCA75R11A294I', 'EJ734LV'),
	(3, '2024-01-16 15:00:00', '2024-01-16 19:00:00', '2024-01-16 16:00:00', 'BNCLRA80A01H703E', 'BNCLCA75R11A294I', 'EJ734LV');


--
-- Name: corriere_idsede_seq; Type: SEQUENCE SET; Schema: uninadelivery; Owner: postgres
--

SELECT pg_catalog.setval('uninadelivery.corriere_idsede_seq', 1, false);


--
-- Name: dettagli_ordine_idordine_seq; Type: SEQUENCE SET; Schema: uninadelivery; Owner: postgres
--

SELECT pg_catalog.setval('uninadelivery.dettagli_ordine_idordine_seq', 1, false);


--
-- Name: mezzo_di_trasporto_idsede_seq; Type: SEQUENCE SET; Schema: uninadelivery; Owner: postgres
--

SELECT pg_catalog.setval('uninadelivery.mezzo_di_trasporto_idsede_seq', 1, false);


--
-- Name: operatore_idsede_seq; Type: SEQUENCE SET; Schema: uninadelivery; Owner: postgres
--

SELECT pg_catalog.setval('uninadelivery.operatore_idsede_seq', 1, false);


--
-- Name: ordine_idordine_seq; Type: SEQUENCE SET; Schema: uninadelivery; Owner: postgres
--

SELECT pg_catalog.setval('uninadelivery.ordine_idordine_seq', 47, true);


--
-- Name: ordine_idspedizione_seq; Type: SEQUENCE SET; Schema: uninadelivery; Owner: postgres
--

SELECT pg_catalog.setval('uninadelivery.ordine_idspedizione_seq', 1, false);


--
-- Name: prodotto_idprodotto_seq; Type: SEQUENCE SET; Schema: uninadelivery; Owner: postgres
--

SELECT pg_catalog.setval('uninadelivery.prodotto_idprodotto_seq', 1, false);


--
-- Name: sede_id_sede_seq; Type: SEQUENCE SET; Schema: uninadelivery; Owner: postgres
--

SELECT pg_catalog.setval('uninadelivery.sede_id_sede_seq', 4, true);


--
-- Name: spedizione_idspedizione_seq; Type: SEQUENCE SET; Schema: uninadelivery; Owner: postgres
--

SELECT pg_catalog.setval('uninadelivery.spedizione_idspedizione_seq', 1, false);


--
-- Name: acquirente acquirente_pkey; Type: CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.acquirente
    ADD CONSTRAINT acquirente_pkey PRIMARY KEY (email);


--
-- Name: corriere corriere_pkey; Type: CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.corriere
    ADD CONSTRAINT corriere_pkey PRIMARY KEY (codicefiscale);


--
-- Name: dettagli_ordine dettagli_ordine_pkey; Type: CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.dettagli_ordine
    ADD CONSTRAINT dettagli_ordine_pkey PRIMARY KEY (idordine, idprodotto);


--
-- Name: disponibilità disponibilità_pkey; Type: CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery."disponibilità"
    ADD CONSTRAINT "disponibilità_pkey" PRIMARY KEY (idprodotto, idsede);


--
-- Name: operatore email_operatore_unica; Type: CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.operatore
    ADD CONSTRAINT email_operatore_unica UNIQUE (email);


--
-- Name: metodo_pagamento metodo_pagamento_pkey; Type: CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.metodo_pagamento
    ADD CONSTRAINT metodo_pagamento_pkey PRIMARY KEY (numerocarta);


--
-- Name: mezzo_di_trasporto mezzo_di_trasporto_pkey; Type: CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.mezzo_di_trasporto
    ADD CONSTRAINT mezzo_di_trasporto_pkey PRIMARY KEY (targa);


--
-- Name: operatore operatore_pkey; Type: CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.operatore
    ADD CONSTRAINT operatore_pkey PRIMARY KEY (codicefiscale);


--
-- Name: ordine ordine_pkey; Type: CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.ordine
    ADD CONSTRAINT ordine_pkey PRIMARY KEY (idordine);


--
-- Name: prodotto prodotto_pkey; Type: CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.prodotto
    ADD CONSTRAINT prodotto_pkey PRIMARY KEY (idprodotto);


--
-- Name: sede sede_pkey; Type: CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.sede
    ADD CONSTRAINT sede_pkey PRIMARY KEY (idsede);


--
-- Name: spedizione spedizione_pkey; Type: CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.spedizione
    ADD CONSTRAINT spedizione_pkey PRIMARY KEY (idspedizione);


--
-- Name: acquirente acquirente_eliminato; Type: TRIGGER; Schema: uninadelivery; Owner: postgres
--

CREATE TRIGGER acquirente_eliminato BEFORE DELETE ON uninadelivery.acquirente FOR EACH ROW EXECUTE FUNCTION uninadelivery.acquirente_eliminato();


--
-- Name: dettagli_ordine dettagli_ordine_eliminato; Type: TRIGGER; Schema: uninadelivery; Owner: postgres
--

CREATE TRIGGER dettagli_ordine_eliminato BEFORE DELETE ON uninadelivery.dettagli_ordine FOR EACH ROW EXECUTE FUNCTION uninadelivery.non_cancellare_dettagli_ordini();


--
-- Name: dettagli_ordine dettagli_ordine_modificato_attributi_costanti; Type: TRIGGER; Schema: uninadelivery; Owner: postgres
--

CREATE TRIGGER dettagli_ordine_modificato_attributi_costanti BEFORE UPDATE ON uninadelivery.dettagli_ordine FOR EACH STATEMENT EXECUTE FUNCTION uninadelivery.impossibile_modificare_dettagli_ordine();


--
-- Name: dettagli_ordine inserimento_dettagli_ordine; Type: TRIGGER; Schema: uninadelivery; Owner: postgres
--

CREATE TRIGGER inserimento_dettagli_ordine BEFORE INSERT ON uninadelivery.dettagli_ordine FOR EACH ROW EXECUTE FUNCTION uninadelivery.nuovi_dettagli_per_ordine();


--
-- Name: ordine ordine_aggiunto_a_spedizione; Type: TRIGGER; Schema: uninadelivery; Owner: postgres
--

CREATE TRIGGER ordine_aggiunto_a_spedizione BEFORE INSERT OR UPDATE OF idspedizione ON uninadelivery.ordine FOR EACH ROW EXECUTE FUNCTION uninadelivery.controllo_capienza_e_tempi_ordine_aggiunto_a_spedizione();


--
-- Name: ordine ordine_annullato; Type: TRIGGER; Schema: uninadelivery; Owner: postgres
--

CREATE TRIGGER ordine_annullato BEFORE UPDATE OF stato ON uninadelivery.ordine FOR EACH ROW EXECUTE FUNCTION uninadelivery.annulla_ordine();


--
-- Name: ordine ordine_modificato_attributi_costanti; Type: TRIGGER; Schema: uninadelivery; Owner: postgres
--

CREATE TRIGGER ordine_modificato_attributi_costanti BEFORE UPDATE OF data, orarioinizio, orariofine, dataeffettuazione, idsede, cap, "città", via, civico, edificio ON uninadelivery.ordine FOR EACH ROW EXECUTE FUNCTION uninadelivery.impossibile_modificare_attributo_ordine();


--
-- Name: ordine ordine_spedito; Type: TRIGGER; Schema: uninadelivery; Owner: postgres
--

CREATE TRIGGER ordine_spedito BEFORE UPDATE OF idspedizione ON uninadelivery.ordine FOR EACH ROW EXECUTE FUNCTION uninadelivery.controlla_ordine_spedito();


--
-- Name: spedizione spedizione_inserita_o_aggiornata_controllo_disponibilita; Type: TRIGGER; Schema: uninadelivery; Owner: postgres
--

CREATE TRIGGER spedizione_inserita_o_aggiornata_controllo_disponibilita BEFORE INSERT OR UPDATE ON uninadelivery.spedizione FOR EACH ROW EXECUTE FUNCTION uninadelivery.controlla_intervalli_spedizione();


--
-- Name: spedizione spedizione_inserita_o_aggiornata_controllo_idsede; Type: TRIGGER; Schema: uninadelivery; Owner: postgres
--

CREATE TRIGGER spedizione_inserita_o_aggiornata_controllo_idsede BEFORE INSERT OR UPDATE ON uninadelivery.spedizione FOR EACH ROW EXECUTE FUNCTION uninadelivery.controlla_idsede_spedizione();


--
-- Name: corriere corriere_idsede_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.corriere
    ADD CONSTRAINT corriere_idsede_fkey FOREIGN KEY (idsede) REFERENCES uninadelivery.sede(idsede);


--
-- Name: dettagli_ordine dettagli_ordine_idordine_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.dettagli_ordine
    ADD CONSTRAINT dettagli_ordine_idordine_fkey FOREIGN KEY (idordine) REFERENCES uninadelivery.ordine(idordine);


--
-- Name: dettagli_ordine dettagli_ordine_idprodotto_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.dettagli_ordine
    ADD CONSTRAINT dettagli_ordine_idprodotto_fkey FOREIGN KEY (idprodotto) REFERENCES uninadelivery.prodotto(idprodotto);


--
-- Name: disponibilità disponibilità_idprodotto_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery."disponibilità"
    ADD CONSTRAINT "disponibilità_idprodotto_fkey" FOREIGN KEY (idprodotto) REFERENCES uninadelivery.prodotto(idprodotto);


--
-- Name: disponibilità disponibilità_idsede_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery."disponibilità"
    ADD CONSTRAINT "disponibilità_idsede_fkey" FOREIGN KEY (idsede) REFERENCES uninadelivery.sede(idsede);


--
-- Name: metodo_pagamento metodo_pagamento_email_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.metodo_pagamento
    ADD CONSTRAINT metodo_pagamento_email_fkey FOREIGN KEY (email) REFERENCES uninadelivery.acquirente(email) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: mezzo_di_trasporto mezzo_di_trasporto_idsede_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.mezzo_di_trasporto
    ADD CONSTRAINT mezzo_di_trasporto_idsede_fkey FOREIGN KEY (idsede) REFERENCES uninadelivery.sede(idsede);


--
-- Name: operatore operatore_idsede_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.operatore
    ADD CONSTRAINT operatore_idsede_fkey FOREIGN KEY (idsede) REFERENCES uninadelivery.sede(idsede);


--
-- Name: ordine ordine_emailacquirente_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.ordine
    ADD CONSTRAINT ordine_emailacquirente_fkey FOREIGN KEY (emailacquirente) REFERENCES uninadelivery.acquirente(email);


--
-- Name: ordine ordine_idsede_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.ordine
    ADD CONSTRAINT ordine_idsede_fkey FOREIGN KEY (idsede) REFERENCES uninadelivery.sede(idsede);


--
-- Name: ordine ordine_idspedizione_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.ordine
    ADD CONSTRAINT ordine_idspedizione_fkey FOREIGN KEY (idspedizione) REFERENCES uninadelivery.spedizione(idspedizione);


--
-- Name: spedizione spedizione_codicefiscalecorriere_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.spedizione
    ADD CONSTRAINT spedizione_codicefiscalecorriere_fkey FOREIGN KEY (codicefiscalecorriere) REFERENCES uninadelivery.corriere(codicefiscale);


--
-- Name: spedizione spedizione_codicefiscaleoperatore_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.spedizione
    ADD CONSTRAINT spedizione_codicefiscaleoperatore_fkey FOREIGN KEY (codicefiscaleoperatore) REFERENCES uninadelivery.operatore(codicefiscale);


--
-- Name: spedizione spedizione_targa_fkey; Type: FK CONSTRAINT; Schema: uninadelivery; Owner: postgres
--

ALTER TABLE ONLY uninadelivery.spedizione
    ADD CONSTRAINT spedizione_targa_fkey FOREIGN KEY (targa) REFERENCES uninadelivery.mezzo_di_trasporto(targa);


--
-- PostgreSQL database dump complete
--

